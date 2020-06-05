package featherkraken.airports.boundary;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import featherkraken.airports.kiwi.control.KiwiAirportLookup;
import featherkraken.flights.entity.Airport;

@Path(AirportResource.PATH)
@RequestScoped
public class AirportResource
{

    public static final String PATH = "airports";

    @GET
    @Produces(APPLICATION_JSON)
    public Response getAirports(@QueryParam("query") String query)
    {
        if (query == null || query.length() < 2) {
            throw new BadRequestException("Query string too short.");
        }
        return Response.ok(new GenericEntity<List<Airport>>(KiwiAirportLookup.executeSearch(query)) {}).build();
    }
}
