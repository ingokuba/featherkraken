package featherkraken.flights.kiwi.control;

import static featherkraken.flights.kiwi.control.KiwiConnector.TEQUILA_API_KEY;
import static featherkraken.flights.test.EntityBuilder.fullAirport;
import static featherkraken.flights.test.EntityBuilder.fullSearchRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import featherkraken.flights.entity.SearchResult;

class KiwiConnectorIT
{

    @AfterEach
    void setApiKey()
    {
        System.setProperty(TEQUILA_API_KEY, System.getenv("TEQUILA_API_KEY"));
    }

    @Test
    void should_return_null_if_apiKey_is_invalid()
    {
        System.setProperty(TEQUILA_API_KEY, "invalid");

        SearchResult result = new KiwiConnector().search(Arrays.asList(fullAirport()), fullSearchRequest());

        assertThat(result, nullValue());
    }

    @Test
    void should_return_null_if_apiKey_is_missing()
    {
        System.clearProperty(TEQUILA_API_KEY);

        SearchResult result = new KiwiConnector().search(Arrays.asList(fullAirport()), fullSearchRequest());

        assertThat(result, nullValue());
    }
}
