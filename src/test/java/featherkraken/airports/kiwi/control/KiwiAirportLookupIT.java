package featherkraken.airports.kiwi.control;

import static featherkraken.kiwi.control.KiwiUtil.TEQUILA_API_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import featherkraken.flights.entity.Airport;

class KiwiAirportLookupIT
{

    @AfterEach
    void setApiKey()
    {
        System.setProperty(TEQUILA_API_KEY, System.getenv("TEQUILA_API_KEY"));
    }

    @Test
    void should_return_empty_list_if_apiKey_is_invalid()
    {
        System.setProperty(TEQUILA_API_KEY, "invalid");

        List<Airport> airports = KiwiAirportLookup.executeSearch("FRA");

        assertThat(airports, empty());
    }

    @Test
    void should_return_empty_list_if_apiKey_is_missing()
    {
        System.clearProperty(TEQUILA_API_KEY);

        List<Airport> airports = KiwiAirportLookup.executeSearch("FRA");

        assertThat(airports, empty());
    }
}
