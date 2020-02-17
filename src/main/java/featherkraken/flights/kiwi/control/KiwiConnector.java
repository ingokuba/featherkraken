package featherkraken.flights.kiwi.control;

import static featherkraken.flights.control.JsonUtil.toStringList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.text.SimpleDateFormat;
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

public class KiwiConnector
    implements APIConnector
{

    private static final String ENDPOINT = "https://api.skypicker.com/flights";

    @Override
    public List<Flight> search(SearchRequest request)
    {
        Response response = ClientBuilder.newClient().target(ENDPOINT)
            .queryParam("partner", "picky")
            .queryParam("v", 3)
            .queryParam("curr", "EUR")
            .queryParam("fly_from", request.getSource())
            .queryParam("fly_to", request.getTarget())
            .queryParam("date_from", dateFormat(request.getDeparture()))
            .queryParam("date_to", dateFormat(request.getDeparture()))
            .queryParam("return_from", dateFormat(request.getReturn()))
            .queryParam("return_to", dateFormat(request.getReturn()))
            .queryParam("selected_cabins", parseClass(request.getClassType()))
            .queryParam("flight_type", request.getTripType() == TripType.ONE_WAY ? "oneway" : "round")
            .request(APPLICATION_JSON_TYPE).get();
        List<Flight> flights = new ArrayList<>();
        JsonObject json = response.readEntity(JsonObject.class);
        JsonValue data = json.get("data");
        if (data == null) {
            return flights;
        }
        JsonArray jsonFlights = (JsonArray)data;
        jsonFlights.forEach(flight -> flights.add(parseFlight((JsonObject)flight)));
        return flights;
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
    private Flight parseFlight(JsonObject flight)
    {
        JsonArray route = flight.getJsonArray("route");
        return new Flight().setPrice(flight.getInt("price", -1))
            .setAirlines(toStringList(flight.getJsonArray("airlines")))
            .setDuration(flight.getString("fly_duration"))
            .setDeparture(new Date(flight.getInt("dTime")))
            .setArrival(new Date(flight.getInt("aTime")))
            .setLink(flight.getString("deep_link"))
            .setStops(route.size() - 1)
            .setRoute(parseRoute(route));
    }

    /**
     * Parse route from Kiwi response to {@link Route}.
     */
    private List<Route> parseRoute(JsonArray array)
    {
        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject kiwiRoute = array.getJsonObject(i);
            Route route = new Route()
                .setSource(new Airport()
                    .setName(kiwiRoute.getString("cityCodeFrom"))
                    .setDisplayName(kiwiRoute.getString("cityFrom")))
                .setTarget(new Airport()
                    .setName(kiwiRoute.getString("cityCodeTo"))
                    .setDisplayName(kiwiRoute.getString("cityTo")))
                .setAirline(kiwiRoute.getString("airline"))
                .setDeparture(new Date(kiwiRoute.getInt("dTime")))
                .setArrival(new Date(kiwiRoute.getInt("aTime")));
            routes.add(route);
        }
        return routes;
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
