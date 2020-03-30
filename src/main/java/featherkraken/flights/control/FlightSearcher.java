package featherkraken.flights.control;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import featherkraken.airports.control.AirportFinder;
import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchResult;
import featherkraken.flights.entity.Trip;
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
     * @return List of found trips or empty list.
     */
    public static SearchResult search(SearchRequest request)
    {
        List<Airport> sourceAirports = AirportFinder.findAirports(request.getSource(), request.getRadius());
        List<Airport> foundSources = new ArrayList<>();
        List<Trip> trips = new ArrayList<>();
        for (APIConnector connector : CONNECTORS) {
            try {
                SearchResult result = connector.search(sourceAirports, request);
                if (result == null) {
                    continue;
                }
                foundSources.addAll(result.getSourceAirports());
                trips.addAll(result.getTrips());
            } catch (Exception e) {
                log.log(Level.WARNING, "Error in " + connector.getClass().getSimpleName(), e);
            }
        }
        return new SearchResult().setSourceAirports(foundSources).setTrips(trips);
    }
}
