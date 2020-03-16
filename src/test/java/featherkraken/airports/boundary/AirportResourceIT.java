package featherkraken.airports.boundary;

import static featherkraken.airports.boundary.AirportResource.PATH;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import featherkraken.flights.entity.Airport;
import featherkraken.flights.test.JerseyResourceProvider;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@ExtendWith(JerseyResourceProvider.class)
public class AirportResourceIT
{

    private JerseyResourceProvider featherkraken;

    @Test
    public void should_return_valid_airports()
    {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("query", "FRA");
        Response response = featherkraken.doGet(PATH, queryParams);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Airport[] airports = response.readEntity(Airport[].class);
        assertThat(airports.length, greaterThanOrEqualTo(1));
        assertThat(airports.length, lessThanOrEqualTo(10));
        Airport airport = airports[0];
        assertThat(airport.getName(), not(emptyOrNullString()));
        assertThat(airport.getDisplayName(), not(emptyOrNullString()));
        assertThat(airport.getLatitude(), anyOf(lessThan(0.0), greaterThan(0.0)));
        assertThat(airport.getLongitude(), anyOf(lessThan(0.0), greaterThan(0.0)));
    }

    @Test
    public void should_return_empty_list_for_unknown_query()
    {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("query", "isadjoasidjas9j");
        Response response = featherkraken.doGet(PATH, queryParams);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Airport[] airports = response.readEntity(Airport[].class);
        assertThat(airports, emptyArray());
    }

    @Test
    public void should_return_error_for_missing_query()
    {
        Response response = featherkraken.doGet(PATH);

        assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void should_return_error_for_short_query()
    {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("query", "a");
        Response response = featherkraken.doGet(PATH, queryParams);

        assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    }
}
