package featherkraken.flights.boundary;

import static featherkraken.flights.boundary.FlightResource.PATH;
import static featherkraken.flights.test.EntityBuilder.fromToday;
import static featherkraken.flights.test.EntityBuilder.fullAirport;
import static featherkraken.flights.test.EntityBuilder.fullSearchRequest;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.util.Calendar;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchRequest.ClassType;
import featherkraken.flights.entity.SearchRequest.TripType;
import featherkraken.flights.entity.SearchResult;
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
class FlightResourceIT
{

    private JerseyResourceProvider featherkraken;

    @Test
    void should_return_valid_flights_for_oneway()
    {
        SearchRequest request = fullSearchRequest()
            .setTripType(TripType.ONE_WAY);

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        SearchResult result = response.readEntity(SearchResult.class);
        List<Trip> trips = result.getTrips();
        assertThat(trips, hasSize(1));
        FlightChecker.check(trips.get(0).getOutwardFlight());
        assertThat(trips.get(0).getReturnFlight(), nullValue());
    }

    @Test
    void should_return_valid_flights_for_round_trip()
    {
        SearchRequest request = fullSearchRequest();

        Response response = featherkraken.doPost(PATH, request);

        checkResult(response);
    }

    @Test
    void should_be_failsafe()
    {
        SearchRequest request = new SearchRequest();

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    @Test
    void should_return_valid_flights_with_radius_filter()
    {
        SearchRequest request = fullSearchRequest()
            .setLimit(100).setRadius(500).setSource(fullAirport());

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        SearchResult result = response.readEntity(SearchResult.class);
        assertThat(result.getSourceAirports().size(), greaterThan(1));
        List<Trip> trips = result.getTrips();
        FlightChecker.check(trips.get(0).getOutwardFlight());
        FlightChecker.check(trips.get(0).getReturnFlight());
    }

    @Test
    void should_return_valid_flights_with_stops()
    {
        SearchRequest request = fullSearchRequest().setStops(1);

        Response response = featherkraken.doPost(PATH, request);

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        SearchResult result = response.readEntity(SearchResult.class);
        List<Trip> trips = result.getTrips();
        assertThat(trips, hasSize(1));
        Flight outwardFlight = trips.get(0).getOutwardFlight();
        FlightChecker.check(outwardFlight);
        assertThat(outwardFlight.getRoute(), hasSize(2));
    }

    @Test
    void should_return_valid_flights_for_date_timespans()
    {
        SearchRequest request = fullSearchRequest()
            .setDeparture(new Timespan().setFrom(fromToday(30, Calendar.DATE)).setTo(fromToday(33, Calendar.DATE)))
            .setReturn(new Timespan().setFrom(fromToday(60, Calendar.DATE)).setTo(fromToday(63, Calendar.DATE)));

        Response response = featherkraken.doPost(PATH, request);

        checkResult(response);
    }

    @Test
    void should_return_valid_flights_for_premium_economy()
    {
        SearchRequest request = fullSearchRequest()
            .setClassType(ClassType.PREMIUM_ECONOMY);

        Response response = featherkraken.doPost(PATH, request);

        checkResult(response);
    }

    @Test
    void should_return_valid_flights_for_business_class()
    {
        SearchRequest request = fullSearchRequest()
            .setClassType(ClassType.BUSINESS);

        Response response = featherkraken.doPost(PATH, request);

        checkResult(response);
    }

    @Test
    void should_return_valid_flights_for_first_class()
    {
        SearchRequest request = fullSearchRequest()
            .setClassType(ClassType.FIRST_CLASS);

        Response response = featherkraken.doPost(PATH, request);

        checkResult(response);
    }

    /**
     * Checks that response has status 200 OK and result has at least 1 trip.
     */
    private void checkResult(Response response)
    {
        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        SearchResult result = response.readEntity(SearchResult.class);
        List<Trip> trips = result.getTrips();
        assertThat(trips, hasSize(1));
        FlightChecker.check(trips.get(0).getOutwardFlight());
        FlightChecker.check(trips.get(0).getReturnFlight());
    }
}
