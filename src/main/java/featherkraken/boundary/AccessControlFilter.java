package featherkraken.boundary;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
public class AccessControlFilter
    implements ContainerResponseFilter
{

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
        throws IOException
    {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        insert(headers, "Access-Control-Allow-Origin", "*");
        insert(headers, "Access-Control-Allow-Credentials", "true");
        insert(headers, "Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        insert(headers, "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }

    private void insert(MultivaluedMap<String, Object> headers, String key, String value)
    {
        if (!headers.containsKey(key)) {
            headers.add(key, value);
        }
    }
}
