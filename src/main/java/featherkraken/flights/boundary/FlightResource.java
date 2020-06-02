package featherkraken.flights.boundary;

import static featherkraken.flights.error.ErrorUtil.NO_ENTITY;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import featherkraken.flights.control.FlightSearcher;
import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.SearchRequest;
import lombok.extern.java.Log;

@Log
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
        logRequest(request);
        return Response.ok(FlightSearcher.search(request)).build();
    }

    private static void logRequest(SearchRequest request)
    {
        Airport source = request.getSource();
        Integer radius = request.getRadius();
        Airport target = request.getTarget();
        log.info(format("Searching flights from %1$s (+%2$dkm) to %3$s.",
                        source != null ? source.getName() : "<unknown>",
                        radius != null ? radius : 0,
                        target != null ? target.getName() : "<unknown>"));
    }
}
