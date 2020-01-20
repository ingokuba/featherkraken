package featherkraken.flights.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper
    implements ExceptionMapper<WebApplicationException>
{

    @Override
    public Response toResponse(WebApplicationException exception)
    {
        ErrorResponse error = ErrorResponse.builder().error(fromWebApplicationException(exception)).build();
        return Response.fromResponse(exception.getResponse()).entity(error).build();
    }

    /**
     * Maps the status code and message of a {@link WebApplicationException} to an error object.
     * 
     * @param wae Supplies status code and message for the error.
     * @return {@link Error} Dto object.
     */
    private static Error fromWebApplicationException(WebApplicationException wae)
    {
        return Error.builder().name(wae.getClass().getName()).message(wae.getMessage()).build();
    }
}