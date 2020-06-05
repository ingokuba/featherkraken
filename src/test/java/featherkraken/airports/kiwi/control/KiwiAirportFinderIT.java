package featherkraken.airports.kiwi.control;

import static featherkraken.flights.test.EntityBuilder.fullAirport;
import static featherkraken.kiwi.control.KiwiUtil.TEQUILA_API_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import featherkraken.flights.entity.Airport;

/**
 * External API test for {@link AirportFinder}.
 */
class KiwiAirportFinderIT
{

    @BeforeEach
    @AfterEach
    void setApiKey()
    {
        System.setProperty(TEQUILA_API_KEY, System.getenv("TEQUILA_API_KEY"));
    }

    @Test
    void should_find_airports_with_given_radius()
    {
        List<Airport> airports = KiwiAirportFinder.findAirports(fullAirport(), 500);

        assertThat(airports.size(), greaterThan(1));
    }

    @Test
    void should_return_single_airport_if_radius_is_zero()
    {
        Airport source = fullAirport();

        List<Airport> airports = KiwiAirportFinder.findAirports(source, 0);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    @Test
    void should_return_single_airport_if_radius_is_negative()
    {
        Airport source = fullAirport();

        List<Airport> airports = KiwiAirportFinder.findAirports(source, -10);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    @Test
    void should_return_single_airport_if_apiKey_is_invalid()
    {
        System.setProperty(TEQUILA_API_KEY, "invalid");
        Airport source = fullAirport();

        List<Airport> airports = KiwiAirportFinder.findAirports(source, 500);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    @Test
    void should_return_single_airport_if_apiKey_is_missing()
    {
        System.clearProperty(TEQUILA_API_KEY);
        Airport source = fullAirport();

        List<Airport> airports = KiwiAirportFinder.findAirports(source, 500);

        assertThat(airports, hasSize(1));
        assertThat(airports.get(0), equalTo(source));
    }

    @Test
    void should_find_airport_LEJ_from_MUC()
    {
        Airport munich = new Airport()
            .setName("MUC").setDisplayName("Munich")
            .setLatitude(48.353889).setLongitude(11.786111);

        List<Airport> airports = KiwiAirportFinder.findAirports(munich, 350);

        assertThat(airports, hasItem(hasProperty(Airport.Fields.name, equalTo("LEJ"))));
    }
}
