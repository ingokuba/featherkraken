package featherkraken.boundary;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationScoped
@ApplicationPath("rest")
public class FeatherkrakenApplication extends Application
{

    @Override
    public Map<String, Object> getProperties()
    {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jersey.config.jsonFeature", "JacksonFeature");
        return properties;
    }
}