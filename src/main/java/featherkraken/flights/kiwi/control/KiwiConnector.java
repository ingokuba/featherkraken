package featherkraken.flights.kiwi.control;

import static featherkraken.flights.control.JsonUtil.toStringList;
import static featherkraken.flights.entity.SearchRequest.TripType.ONE_WAY;
import static featherkraken.flights.entity.SearchRequest.TripType.ROUND_TRIP;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

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
import featherkraken.kiwi.control.KiwiUtil;
import lombok.extern.java.Log;

@Log
public class KiwiConnector
    implements APIConnector
{

    private static final String ENDPOINT       = "https://tequila-api.kiwi.com/v2/search";
    private static final String BOOKING_URL    = "https://www.kiwi.com/booking?token=";

    private DateFormat          kiwiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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
        WebTarget webTarget = ClientBuilder.newClient().target(ENDPOINT)
            .queryParam("curr", "EUR")
            .queryParam("limit", request.getLimit())
            .queryParam("fly_from", source)
            .queryParam("fly_to", request.getTarget().getName())
            .queryParam("selected_cabins", parseClass(request.getClassType()))
            .queryParam("flight_type", request.getTripType() == ONE_WAY ? "oneway" : "round");
        if (request.getStops() != null) {
            webTarget = webTarget.queryParam("max_stopovers", request.getStops());
        }
        Timespan departure = request.getDeparture();
        if (departure != null) {
            webTarget = webTarget
                .queryParam("date_from", dateFormat(departure.getFrom()))
                .queryParam("date_to", dateFormat(departure.getTo()));
        }
        Timespan returnDate = request.getReturn();
        if (returnDate != null) {
            webTarget = webTarget
                .queryParam("return_from", dateFormat(returnDate.getFrom()))
                .queryParam("return_to", dateFormat(returnDate.getTo()));
        }
        Response response = webTarget.request(APPLICATION_JSON_TYPE).header("apikey", apiKey).get();
        StatusType status = response.getStatusInfo();
        if (!OK.equals(status.toEnum())) {
            log.severe(format("Response code was: %1$d %2$s", status.getStatusCode(), status.getReasonPhrase()));
            return null;
        }
        List<Trip> trips = new ArrayList<>();
        JsonObject json = response.readEntity(JsonObject.class);
        JsonValue data = json.get("data");
        if (data == null) {
            return null;
        }
        JsonArray jsonFlights = (JsonArray)data;
        jsonFlights.forEach(flight -> trips.add(parseTrip((JsonObject)flight, request.getTripType())));
        return new SearchResult().setSourceAirports(foundSources).setTrips(trips);
    }

    /**
     * Parses a date to the correct format expected by Kiwi.com.
     */
    private String dateFormat(Date date)
    {
        if (date != null) {
            return new SimpleDateFormat("dd/MM/yyyy").format(date);
        }
        return null;
    }

    /**
     * Parse json response object to {@link Flight} object.
     */
    private Trip parseTrip(JsonObject object, TripType tripType)
    {
        Trip trip = new Trip().setPrice(object.getInt("price", -1))
            .setAirlines(toStringList(object.getJsonArray("airlines")))
            .setLink(BOOKING_URL + object.getString("booking_token"));
        JsonArray kiwiRoutes = object.getJsonArray("route");
        JsonObject duration = object.getJsonObject("duration");
        Flight outwardFlight = new Flight().setDuration(toTimeString(duration.getInt("departure")))
            .setDeparture(getDate(object.getString("local_departure")))
            .setArrival(getDate(object.getString("local_arrival")));
        Flight returnFlight = ROUND_TRIP.equals(tripType) ? new Flight().setDuration(toTimeString(duration.getInt("return"))) : null;
        for (int i = 0; i < kiwiRoutes.size(); i++) {
            JsonObject kiwiRoute = kiwiRoutes.getJsonObject(i);
            Airport source = parseAirport(kiwiRoute, "From");
            if (i == 0 && !foundSources.contains(source)) {
                foundSources.add(source);
            }
            Airport target = parseAirport(kiwiRoute, "To");
            if (ROUND_TRIP.equals(tripType) && i == kiwiRoutes.size() - 1 && !foundSources.contains(target)) {
                foundSources.add(target);
            }
            Route route = new Route()
                .setSource(source)
                .setTarget(target)
                .setAirline(kiwiRoute.getString("airline"))
                .setDeparture(getDate(kiwiRoute.getString("local_departure")))
                .setArrival(getDate(kiwiRoute.getString("local_arrival")));
            if (kiwiRoute.getInt("return") == 0) {
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
     * Parse Kiwi json route to {@link Airport} object.
     * 
     * @param direction either "From" or "To".
     */
    private Airport parseAirport(JsonObject route, String direction)
    {
        return new Airport()
            .setName(route.getString("cityCode" + direction))
            .setDisplayName(route.getString("city" + direction));
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
     * Parse date from kiwi date string.
     */
    private Date getDate(String date)
    {
        try {
            return kiwiDateFormat.parse(date);
        } catch (ParseException e) {
            log.severe("Couldn't parse date: " + date);
            return null;
        }
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
