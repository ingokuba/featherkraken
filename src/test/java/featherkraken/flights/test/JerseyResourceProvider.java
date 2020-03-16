package featherkraken.flights.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jboss.resteasy.plugins.providers.jsonp.JsonObjectProvider;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import lombok.NoArgsConstructor;

/**
 * Send http requests to a target url.
 */
@NoArgsConstructor
public class JerseyResourceProvider
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver
{

    private JerseyResource resources = new JerseyResource();

    @Override
    public void beforeEach(ExtensionContext context)
        throws Exception
    {
        resources.setUp();
    }

    @Override
    public void afterEach(ExtensionContext context)
        throws Exception
    {
        resources.tearDown();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return parameterContext.getParameter().getType().equals(JerseyResourceProvider.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return this;
    }

    public class JerseyResource extends JerseyTest
    {

        @Override
        protected Application configure()
        {
            ResourceConfig config = new ResourceConfig().packages("featherkraken");
            config.registerClasses(JsonObjectProvider.class, JacksonJaxbJsonProvider.class);
            return config;
        }
    }

    /**
     * Post the data in JSON format.
     *
     * @return the response
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Response doPost(String path, Object object)
    {
        return resources.target(path).request().post(object == null ? null : Entity.entity(object, MediaType.APPLICATION_JSON));
    }

    /**
     * Get without query parameters.
     */
    public Response doGet(String path)
    {
        return doGet(path, new HashMap<>());
    }

    /**
     * Get with query parameters.
     */
    public Response doGet(String path, Map<String, Object> queryParams)
    {
        WebTarget target = resources.target(path);
        for (String key : queryParams.keySet()) {
            target = target.queryParam(key, queryParams.get(key));
        }
        return target.request().get();
    }
}
