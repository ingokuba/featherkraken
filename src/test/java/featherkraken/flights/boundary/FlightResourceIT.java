package featherkraken.flights.boundary;

import static featherkraken.flights.boundary.FlightResource.PATH;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchRequest.ClassType;
import featherkraken.flights.entity.SearchRequest.TripType;
import featherkraken.flights.kiwi.control.KiwiConnector;
import featherkraken.flights.test.FlightChecker;
import featherkraken.flights.test.JerseyResourceProvider;
import lombok.AllArgsConstructor;

/**
 * External API test for {@link KiwiConnector}.
 */
@AllArgsConstructor
@ExtendWith(JerseyResourceProvider.class)
public class FlightResourceIT
{

    private JerseyResourceProvider featherkraken;

    @Test
    public void should_return_valid_flights()
    {
        SearchRequest request = new SearchRequest()
            .setTripType(TripType.ONE_WAY)
            .setClassType(ClassType.ECONOMY)
            .setPassengers(1)
            .setSource("FRA")
            .setTarget("LAX")
            .setDeparture(fromToday(1, Calendar.MONTH));

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Flight[] flights = response.readEntity(Flight[].class);
        assertThat(flights.length, greaterThan(0));
        FlightChecker.check(flights[0]);
    }

    @Test
    public void should_be_failsafe()
    {
        SearchRequest request = new SearchRequest();

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    private static Date fromToday(int amount, int field)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        return calendar.getTime();
    }
}
