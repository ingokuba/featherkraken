package featherkraken.flights.boundary;

import static featherkraken.flights.error.ErrorUtil.NO_ENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import featherkraken.flights.control.FlightSearcher;
import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.SearchRequest;

@Path(FlightResource.PATH)
@RequestScoped
public class FlightResource
{

    public static final String PATH = "flights";

    @POST
    @Produces(APPLICATION_JSON)
    public Response search(SearchRequest request)
    {
        if (request == null) {
            throw new BadRequestException(NO_ENTITY);
        }
        return Response.status(OK).entity(new GenericEntity<List<Flight>>(FlightSearcher.search(request)) {}).build();
    }
}
