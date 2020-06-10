package featherkraken.flights.kiwi.control;

import static featherkraken.flights.entity.SearchRequest.ClassType.BUSINESS;
import static featherkraken.flights.entity.SearchRequest.ClassType.ECONOMY;
import static featherkraken.flights.entity.SearchRequest.ClassType.FIRST_CLASS;
import static featherkraken.flights.entity.SearchRequest.ClassType.PREMIUM_ECONOMY;
import static featherkraken.flights.entity.SearchRequest.TripType.ONE_WAY;
import static featherkraken.flights.entity.SearchRequest.TripType.ROUND_TRIP;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import featherkraken.flights.control.APIConnector;
import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.Route;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchRequest.ClassType;
import featherkraken.flights.entity.SearchRequest.TripType;
import featherkraken.flights.entity.SearchResult;
import featherkraken.flights.entity.Timespan;
import featherkraken.flights.entity.Trip;
import featherkraken.flights.kiwi.boundary.KiwiApi;
import featherkraken.flights.kiwi.entity.KiwiDuration;
import featherkraken.flights.kiwi.entity.KiwiFlight;
import featherkraken.flights.kiwi.entity.KiwiResponse;
import featherkraken.flights.kiwi.entity.KiwiRoute;
import featherkraken.kiwi.control.KiwiUtil;

public class KiwiConnector
    implements APIConnector
{

    private static final String ENDPOINT    = "https://tequila-api.kiwi.com/v2";
    private static final String BOOKING_URL = "https://www.kiwi.com/booking?token=";

    private List<Airport>       foundSources;

    @Override
    public SearchResult search(List<Airport> sourceAirports, SearchRequest request)
    {
        String apiKey = KiwiUtil.getApiKey();
        if (apiKey == null) {
            return null;
        }
        foundSources = new ArrayList<>();
        String source = sourceAirports.stream().map(Airport::getName).collect(joining(","));
        ClassType classType = request.getClassType();
        Timespan departure = request.getDeparture();
        Timespan returnDate = request.getReturn();
        KiwiApi kiwiApi = RestClientBuilder.newBuilder().baseUri(URI.create(ENDPOINT)).build(KiwiApi.class);
        KiwiResponse kiwiResponse = kiwiApi.getFlights(apiKey, "EUR", request.getLimit(), request.getStops(), source, request.getTarget().getName(),
                                                       request.getTripType() == ONE_WAY ? "oneway" : "round", parseClass(classType), classesBelow(classType),
                                                       departure != null ? departure.getFrom() : null, departure != null ? departure.getTo() : null,
                                                       returnDate != null ? returnDate.getFrom() : null, returnDate != null ? returnDate.getTo() : null);
        List<Trip> trips = new ArrayList<>();
        kiwiResponse.getData().forEach(flight -> trips.add(parseTrip(flight, request.getTripType())));
        return new SearchResult().setSourceAirports(foundSources).setTrips(trips);
    }

    /**
     * Get all classes below given class and parse to comma separated string of kiwi classes.
     */
    private String classesBelow(ClassType classType)
    {
        List<ClassType> mixClasses;
        switch (classType) {
        case FIRST_CLASS:
            mixClasses = asList(BUSINESS, PREMIUM_ECONOMY, ECONOMY);
            break;
        case BUSINESS:
            mixClasses = asList(PREMIUM_ECONOMY, ECONOMY);
            break;
        case PREMIUM_ECONOMY:
            return parseClass(ECONOMY);
        default:
            return null;
        }
        return mixClasses.stream().map(this::parseClass).collect(joining(","));
    }

    /**
     * Parse json response object to {@link Flight} object.
     */
    private Trip parseTrip(KiwiFlight flight, TripType tripType)
    {
        Trip trip = new Trip().setPrice(flight.getPrice())
            .setAirlines(flight.getAirlines())
            .setLink(BOOKING_URL + flight.getBooking_token());
        List<KiwiRoute> kiwiRoutes = flight.getRoute();
        KiwiDuration duration = flight.getDuration();
        Flight outwardFlight = new Flight().setDuration(toTimeString(duration.getDeparture()))
            .setDeparture(flight.getLocal_departure())
            .setArrival(flight.getLocal_arrival());
        Flight returnFlight = ROUND_TRIP.equals(tripType) ? new Flight().setDuration(toTimeString(duration.getReturnTime())) : null;
        for (int i = 0; i < kiwiRoutes.size(); i++) {
            KiwiRoute kiwiRoute = kiwiRoutes.get(i);
            Airport source = new Airport()
                .setName(kiwiRoute.getCityCodeFrom())
                .setDisplayName(kiwiRoute.getCityFrom());
            if (i == 0 && !foundSources.contains(source)) {
                foundSources.add(source);
            }
            Airport target = new Airport()
                .setName(kiwiRoute.getCityCodeTo())
                .setDisplayName(kiwiRoute.getCityTo());
            if (ROUND_TRIP.equals(tripType) && i == kiwiRoutes.size() - 1 && !foundSources.contains(target)) {
                foundSources.add(target);
            }
            Route route = new Route()
                .setSource(source)
                .setTarget(target)
                .setAirline(kiwiRoute.getAirline())
                .setClassType(parseKiwiClass(kiwiRoute.getFare_category()))
                .setDeparture(kiwiRoute.getLocal_departure())
                .setArrival(kiwiRoute.getLocal_arrival());
            if (kiwiRoute.getReturnTime() == 0) {
                outwardFlight.getRoute().add(route);
            }
            else if (returnFlight != null) {
                returnFlight.getRoute().add(route);
            }
        }
        outwardFlight.setStops(outwardFlight.getRoute().size() - 1);
        if (returnFlight != null) {
            List<Route> returnRoute = returnFlight.getRoute();
            returnFlight.setDeparture(returnRoute.get(0).getDeparture())
                .setArrival(returnRoute.get(returnRoute.size() - 1).getArrival())
                .setStops(returnFlight.getRoute().size() - 1);
            trip.setReturnFlight(returnFlight);
        }
        return trip.setOutwardFlight(outwardFlight);
    }

    /**
     * Parse seconds to string with hours and minutes.
     * e.g. 69780 becomes '19h 23m'
     * 
     * @param duration in seconds
     * @return String with hours and minutes
     */
    private String toTimeString(int duration)
    {
        long days = TimeUnit.SECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toSeconds(days);

        long hours = TimeUnit.SECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toSeconds(hours);

        long minutes = TimeUnit.SECONDS.toMinutes(duration);

        if (days > 0) {
            return format("%1$dd %2$dh %3$dm", days, hours, minutes);
        }
        return hours > 0 ? format("%1$dh %2$dm", hours, minutes) : minutes + "m";
    }

    /**
     * Parse Kiwi class to own format.
     */
    private ClassType parseKiwiClass(String kiwiClass)
    {
        if (kiwiClass != null) {
            switch (kiwiClass) {
            case "M":
                return ECONOMY;
            case "W":
                return PREMIUM_ECONOMY;
            case "C":
                return BUSINESS;
            case "F":
                return FIRST_CLASS;
            default:
                return null;
            }
        }
        return null;
    }

    /**
     * Parse class from request to Kiwi class.
     */
    private String parseClass(ClassType classType)
    {
        if (classType != null) {
            switch (classType) {
            case PREMIUM_ECONOMY:
                return "W";
            case BUSINESS:
                return "C";
            case FIRST_CLASS:
                return "F";
            default:
                break;
            }
        }
        return "M";
    }
}
