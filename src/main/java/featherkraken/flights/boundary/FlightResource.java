package featherkraken.flights.boundary;

import static featherkraken.flights.error.ErrorUtil.NO_ENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import featherkraken.flights.control.FlightSearcher;
import featherkraken.flights.entity.SearchRequest;

@Path(FlightResource.PATH)
@RequestScoped
public class FlightResource
{

    public static final String PATH = "flights";

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response search(SearchRequest request)
    {
        if (request == null) {
            throw new BadRequestException(NO_ENTITY);
        }
        return Response.ok(FlightSearcher.search(request)).build();
    }
}
