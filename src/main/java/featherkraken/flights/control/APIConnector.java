package featherkraken.flights.control;

import java.util.List;

import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchResult;

public interface APIConnector
{

    public SearchResult search(List<Airport> sourceAirports, SearchRequest request);
}
