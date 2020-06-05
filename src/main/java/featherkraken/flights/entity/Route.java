package featherkraken.flights.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import featherkraken.flights.entity.SearchRequest.ClassType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Route
{

    private Airport   source;

    private Airport   target;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date      departure;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date      arrival;

    private String    airline;

    private ClassType classType;
}
