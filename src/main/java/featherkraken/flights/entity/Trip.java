package featherkraken.flights.entity;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Trip
{

    private Integer      price;

    private List<String> airlines;

    private String       link;

    private Flight       outwardFlight;

    private Flight       returnFlight;
}
