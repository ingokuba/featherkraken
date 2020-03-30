package featherkraken.flights.entity;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SearchResult
{

    private List<Airport> sourceAirports;

    private List<Trip>    trips;
}
