package featherkraken.flights.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Flight
{

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date        departure;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date        arrival;

    private String      duration;

    private Integer     stops;

    private List<Route> route = new ArrayList<>();
}
