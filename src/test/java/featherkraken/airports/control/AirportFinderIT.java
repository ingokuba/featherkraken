package featherkraken.airports.control;

import static featherkraken.airports.control.AirportFinder.API_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import featherkraken.flights.entity.Airport;

/**
 * External API test for {@link AirportFinder}.
 */
class AirportFinderIT
{

    @BeforeEach
    void setApiKey()
    {
        System.setProperty(API_KEY, System.getenv("RAPIDAPI_KEY"));
    }

    @Test
    void should_find_airports_with_given_radius()
    {
        List<Airport> airports = AirportFinder.findAirports(validSource(), 500);

        assertThat(airports.size(), greaterThan(1));
    }

    @Test
    void should_return_single_airport_if_radius_is_zero()
    {
        Airport source = validSource();

        List<Airport> airports = AirportFinder.findAirports(source, 0);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    @Test
    void should_return_single_airport_if_radius_is_negative()
    {
        Airport source = validSource();

        List<Airport> airports = AirportFinder.findAirports(source, -10);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    @Test
    void should_return_single_airport_if_apiKey_is_invalid()
    {
        System.setProperty(API_KEY, "invalid");
        Airport source = validSource();

        List<Airport> airports = AirportFinder.findAirports(source, 500);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    @Test
    void should_return_single_airport_if_apiKey_is_missing()
    {
        System.clearProperty(API_KEY);
        Airport source = validSource();

        List<Airport> airports = AirportFinder.findAirports(source, 500);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    private Airport validSource()
    {
        return new Airport().setName("FRA").setLatitude(50.033056).setLongitude(8.570556);
    }
}
