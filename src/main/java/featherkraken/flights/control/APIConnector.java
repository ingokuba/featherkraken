package featherkraken.flights.control;

import java.util.List;

import featherkraken.flights.entity.Flight;
import featherkraken.flights.entity.SearchRequest;

public interface APIConnector
{

    public List<Flight> search(SearchRequest request);
}
