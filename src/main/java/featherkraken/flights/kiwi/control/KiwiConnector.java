package featherkraken.flights.kiwi.control;

import static featherkraken.flights.control.JsonUtil.toStringList;
import static featherkraken.flights.entity.SearchRequest.TripType.ONE_WAY;
import static featherkraken.flights.entity.SearchRequest.TripType.ROUND_TRIP;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

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

public class KiwiConnector
    implements APIConnector
{

    private static final String ENDPOINT     = "https://api.skypicker.com/flights";

    private List<Airport>       foundSources = new ArrayList<>();

    @Override
    public SearchResult search(List<Airport> sourceAirports, SearchRequest request)
    {
        String source = sourceAirports.stream().map(Airport::getName).collect(joining(","));
        WebTarget webTarget = ClientBuilder.newClient().target(ENDPOINT)
            .queryParam("partner", "picky")
            .queryParam("v", 3)
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
        Response response = webTarget.request(APPLICATION_JSON_TYPE).get();
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
            .setLink(object.getString("deep_link"));
        JsonArray kiwiRoutes = object.getJsonArray("route");
        Flight outwardFlight = new Flight().setDuration(object.getString("fly_duration"))
            .setDeparture(getDate(object.getInt("dTime")))
            .setArrival(getDate(object.getInt("aTime")));
        Flight returnFlight = ROUND_TRIP.equals(tripType) ? new Flight().setDuration(object.getString("return_duration")) : null;
        for (int i = 0; i < kiwiRoutes.size(); i++) {
            JsonObject kiwiRoute = kiwiRoutes.getJsonObject(i);
            Airport source = parseAirport(kiwiRoute, "From");
            if (i == 0 && !foundSources.contains(source)) {
                foundSources.add(source);
            }
            Route route = new Route()
                .setSource(source)
                .setTarget(parseAirport(kiwiRoute, "To"))
                .setAirline(kiwiRoute.getString("airline"))
                .setDeparture(getDate(kiwiRoute.getInt("dTime")))
                .setArrival(getDate(kiwiRoute.getInt("aTime")));
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
        Airport airport = new Airport()
            .setName(route.getString("cityCode" + direction))
            .setDisplayName(route.getString("city" + direction));
        JsonNumber latitude = route.getJsonNumber("lat" + direction);
        if (latitude != null) {
            airport.setLatitude(latitude.doubleValue());
        }
        JsonNumber longitude = route.getJsonNumber("lng" + direction);
        if (longitude != null) {
            airport.setLongitude(longitude.doubleValue());
        }
        return airport;
    }

    /**
     * Parse date from epoch seconds.
     */
    private Date getDate(int seconds)
    {
        return Date.from(Instant.ofEpochSecond(seconds));
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
