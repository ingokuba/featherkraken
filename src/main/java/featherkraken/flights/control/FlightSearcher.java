package featherkraken.flights.control;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.kiwi.control.KiwiConnector;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = PRIVATE)
public abstract class FlightSearcher
{

    private static final List<APIConnector> CONNECTORS = asList(new KiwiConnector());

    /**
     * Call all connected APIs to search flights.
     * 
     * @param request Object with parameters for the search.
     * @return List of found flights or empty list.
     */
    public static List<Flight> search(SearchRequest request)
    {
        List<Flight> flights = new ArrayList<>();
        for (APIConnector connector : CONNECTORS) {
            try {
                flights.addAll(connector.search(request));
            } catch (Exception e) {
                log.log(Level.WARNING, "Error in " + connector.getClass().getSimpleName(), e);
            }
        }
        return flights;
    }
}
