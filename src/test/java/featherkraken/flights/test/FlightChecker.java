package featherkraken.flights.test;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.hamcrest.Matchers;

import featherkraken.flights.entity.Flight;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class FlightChecker
{

    /**
     * Checks whether the examined flight has all mandatory attributes set.
     */
    public static void check(Flight flight)
    {
        assertThat("should have correct stops", flight.getStops(), equalTo(flight.getRoute().size() - 1));
        assertThat("should have duration", flight.getDuration(), notNullValue());
        assertThat("should have valid departure and arrival time", flight.getDeparture(), Matchers.lessThan(flight.getArrival()));
    }
}
