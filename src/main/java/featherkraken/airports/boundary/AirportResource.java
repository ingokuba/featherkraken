package featherkraken.airports.boundary;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

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

import featherkraken.flights.entity.Airport;

@Path(AirportResource.PATH)
@RequestScoped
public class AirportResource
{

    public static final String  PATH     = "airports";
    private static final String ENDPOINT = "https://api.skypicker.com/locations";

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
        Response response = ClientBuilder.newClient().target(ENDPOINT)
            .queryParam("term", query)
            .queryParam("location_types", "airport")
            .request(APPLICATION_JSON_TYPE).get();
        JsonObject json = response.readEntity(JsonObject.class);
        JsonValue data = json.get("locations");
        List<Airport> airports = new ArrayList<>();
        if (data == null) {
            return airports;
        }
        JsonArray jsonLocations = (JsonArray)data;
        jsonLocations.forEach(location -> airports.add(parseAirport((JsonObject)location)));
        return airports;
    }

    private Airport parseAirport(JsonObject location)
    {
        return new Airport()
            .setName(location.getString("code"))
            .setDisplayName(location.getString("name"));
    }
}
