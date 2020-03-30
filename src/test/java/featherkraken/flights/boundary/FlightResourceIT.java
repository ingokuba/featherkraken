package featherkraken.flights.boundary;

import static featherkraken.flights.boundary.FlightResource.PATH;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchRequest.ClassType;
import featherkraken.flights.entity.SearchRequest.TripType;
import featherkraken.flights.entity.Timespan;
import featherkraken.flights.entity.Trip;
import featherkraken.flights.test.FlightChecker;
import featherkraken.flights.test.JerseyResourceProvider;
import lombok.AllArgsConstructor;

/**
 * External API test for {@link FlightResource}.
 */
@AllArgsConstructor
@ExtendWith(JerseyResourceProvider.class)
public class FlightResourceIT
{

    private JerseyResourceProvider featherkraken;

    @Test
    public void should_return_valid_flights_for_oneway()
    {
        SearchRequest request = new SearchRequest()
            .setLimit(1)
            .setTripType(TripType.ONE_WAY)
            .setClassType(ClassType.ECONOMY)
            .setPassengers(1)
            .setSource(airport("FRA"))
            .setTarget(airport("LAX"))
            .setDeparture(new Timespan().setFrom(fromToday(1, Calendar.MONTH)));

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Trip[] trips = response.readEntity(Trip[].class);
        assertThat(trips.length, equalTo(1));
        FlightChecker.check(trips[0].getOutwardFlight());
        assertThat(trips[0].getReturnFlight(), nullValue());
    }

    @Test
    public void should_return_valid_flights_for_round_trip()
    {
        SearchRequest request = new SearchRequest()
            .setLimit(1)
            .setTripType(TripType.ROUND_TRIP)
            .setClassType(ClassType.ECONOMY)
            .setPassengers(1)
            .setRadius(0)
            .setSource(airport("FRA"))
            .setTarget(airport("LAX"))
            .setDeparture(new Timespan().setFrom(fromToday(1, Calendar.MONTH)))
            .setReturn(new Timespan().setFrom(fromToday(2, Calendar.MONTH)));

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Trip[] trips = response.readEntity(Trip[].class);
        assertThat(trips.length, equalTo(1));
        FlightChecker.check(trips[0].getOutwardFlight());
        FlightChecker.check(trips[0].getReturnFlight());
    }

    @Test
    public void should_be_failsafe()
    {
        SearchRequest request = new SearchRequest();

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    @Test
    public void should_return_valid_flights_with_radius_filter()
    {
        SearchRequest request = new SearchRequest()
            .setLimit(1)
            .setTripType(TripType.ROUND_TRIP)
            .setClassType(ClassType.ECONOMY)
            .setPassengers(1)
            .setRadius(500)
            .setSource(airport("FRA").setLatitude(50.033056).setLongitude(8.570556))
            .setTarget(airport("LAX"))
            .setDeparture(new Timespan().setFrom(fromToday(1, Calendar.MONTH)))
            .setReturn(new Timespan().setFrom(fromToday(2, Calendar.MONTH)));

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Trip[] trips = response.readEntity(Trip[].class);
        assertThat(trips.length, equalTo(1));
        FlightChecker.check(trips[0].getOutwardFlight());
        FlightChecker.check(trips[0].getReturnFlight());
    }

    @Test
    public void should_return_valid_flights_with_stops()
    {
        SearchRequest request = new SearchRequest()
            .setLimit(1)
            .setTripType(TripType.ONE_WAY)
            .setClassType(ClassType.ECONOMY)
            .setPassengers(1)
            .setStops(1)
            .setSource(airport("FRA"))
            .setTarget(airport("LAX"))
            .setDeparture(new Timespan().setFrom(fromToday(1, Calendar.MONTH)));

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Trip[] trips = response.readEntity(Trip[].class);
        assertThat(trips.length, equalTo(1));
        Flight outwardFlight = trips[0].getOutwardFlight();
        FlightChecker.check(outwardFlight);
        assertThat(outwardFlight.getRoute(), hasSize(2));
    }

    @Test
    public void should_return_valid_flights_for_date_timespans()
    {
        SearchRequest request = new SearchRequest()
            .setLimit(1)
            .setTripType(TripType.ROUND_TRIP)
            .setClassType(ClassType.ECONOMY)
            .setPassengers(1)
            .setRadius(0)
            .setSource(airport("FRA"))
            .setTarget(airport("LAX"))
            .setDeparture(new Timespan().setFrom(fromToday(30, Calendar.DATE)).setTo(fromToday(33, Calendar.DATE)))
            .setReturn(new Timespan().setFrom(fromToday(60, Calendar.DATE)).setTo(fromToday(63, Calendar.DATE)));

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        Trip[] trips = response.readEntity(Trip[].class);
        assertThat(trips.length, equalTo(1));
        FlightChecker.check(trips[0].getOutwardFlight());
        FlightChecker.check(trips[0].getReturnFlight());
    }

    private static Airport airport(String name)
    {
        return new Airport().setName(name);
    }

    private static Date fromToday(int amount, int field)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        return calendar.getTime();
    }
}
