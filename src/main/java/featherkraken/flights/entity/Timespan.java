package featherkraken.flights.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Timespan
{

    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date from;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date to;
}
