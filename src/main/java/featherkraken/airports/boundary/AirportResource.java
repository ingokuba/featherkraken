package featherkraken.airports.boundary;

import static featherkraken.flights.kiwi.control.KiwiConnector.TEQUILA_API_KEY;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import featherkraken.flights.entity.Airport;
import lombok.extern.java.Log;

@Log
@Path(AirportResource.PATH)
@RequestScoped
public class AirportResource
{

    public static final String  PATH     = "airports";
    private static final String ENDPOINT = "https://tequila-api.kiwi.com/locations/query";

    @GET
    @Produces(APPLICATION_JSON)
    public Response getAirports(@QueryParam("query") String query)
    {
        if (query == null || query.length() < 2) {
            throw new BadRequestException("Query string too short.");
        }
        return Response.ok(new GenericEntity<List<Airport>>(executeSearch(query)) {}).build();
    }

    private List<Airport> executeSearch(String query)
    {
        List<Airport> airports = new ArrayList<>();
        String apiKey = System.getProperty(TEQUILA_API_KEY);
        if (apiKey == null) {
            log.severe("Property '" + TEQUILA_API_KEY + "' has to be set for the AirportFinder.");
            return airports;
        }
        Response response = ClientBuilder.newClient().target(ENDPOINT)
            .queryParam("term", query)
            .queryParam("location_types", "airport")
            .request(APPLICATION_JSON_TYPE)
            .header("apikey", apiKey).get();
        StatusType status = response.getStatusInfo();
        if (!OK.equals(status.toEnum())) {
            log.severe(format("Response code was: %1$d %2$s", status.getStatusCode(), status.getReasonPhrase()));
            return airports;
        }
        JsonObject json = response.readEntity(JsonObject.class);
        JsonValue data = json.get("locations");
        if (data == null) {
            return airports;
        }
        JsonArray jsonLocations = (JsonArray)data;
        jsonLocations.forEach(location -> airports.add(parseAirport((JsonObject)location)));
        return airports;
    }

    private Airport parseAirport(JsonObject location)
    {
        JsonObject latLon = location.getJsonObject("location");
        return new Airport()
            .setName(location.getString("code"))
            .setDisplayName(location.getString("name"))
            .setLatitude(latLon.getJsonNumber("lat").doubleValue())
            .setLongitude(latLon.getJsonNumber("lon").doubleValue());
    }
}
