package featherkraken.flights.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Flight
{

    private Integer      price;

    private List<String> airlines;

    private String       duration;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date         departure;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date         arrival;

    private Integer      stops;

    private String       link;

    private List<Route>  route;
}
