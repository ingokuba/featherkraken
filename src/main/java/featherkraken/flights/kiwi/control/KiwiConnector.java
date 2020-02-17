package featherkraken.flights.kiwi.control;

import static featherkraken.flights.control.JsonUtil.toStringList;
import static featherkraken.flights.entity.SearchRequest.TripType.ONE_WAY;
import static featherkraken.flights.entity.SearchRequest.TripType.ROUND_TRIP;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import featherkraken.flights.control.APIConnector;
import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.Route;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchRequest.ClassType;
import featherkraken.flights.entity.SearchRequest.TripType;
import featherkraken.flights.entity.Trip;

public class KiwiConnector
    implements APIConnector
{

    private static final String ENDPOINT = "https://api.skypicker.com/flights";

    @Override
    public List<Trip> search(SearchRequest request)
    {
        Response response = ClientBuilder.newClient().target(ENDPOINT)
            .queryParam("partner", "picky")
            .queryParam("v", 3)
            .queryParam("curr", "EUR")
            .queryParam("limit", request.getLimit())
            .queryParam("fly_from", request.getSource())
            .queryParam("fly_to", request.getTarget())
            .queryParam("date_from", dateFormat(request.getDeparture()))
            .queryParam("date_to", dateFormat(request.getDeparture()))
            .queryParam("return_from", dateFormat(request.getReturn()))
            .queryParam("return_to", dateFormat(request.getReturn()))
            .queryParam("selected_cabins", parseClass(request.getClassType()))
            .queryParam("flight_type", request.getTripType() == ONE_WAY ? "oneway" : "round")
            .request(APPLICATION_JSON_TYPE).get();
        List<Trip> trips = new ArrayList<>();
        JsonObject json = response.readEntity(JsonObject.class);
        JsonValue data = json.get("data");
        if (data == null) {
            return trips;
        }
        JsonArray jsonFlights = (JsonArray)data;
        jsonFlights.forEach(flight -> trips.add(parseTrip((JsonObject)flight, request.getTripType())));
        return trips;
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
            Route route = new Route()
                .setSource(new Airport()
                    .setName(kiwiRoute.getString("cityCodeFrom"))
                    .setDisplayName(kiwiRoute.getString("cityFrom")))
                .setTarget(new Airport()
                    .setName(kiwiRoute.getString("cityCodeTo"))
                    .setDisplayName(kiwiRoute.getString("cityTo")))
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
