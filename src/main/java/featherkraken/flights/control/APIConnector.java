package featherkraken.flights.control;

import java.util.List;

import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.Trip;

public interface APIConnector
{

    public List<Trip> search(List<Airport> sourceAirports, SearchRequest request);
}
