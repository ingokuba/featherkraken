package featherkraken.flights.kiwi.entity;

import java.util.Date;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class KiwiFlight
{

    private Integer         price;

    private List<String>    airlines;

    private String          booking_token;

    private List<KiwiRoute> route;

    private KiwiDuration    duration;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date            local_departure;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date            local_arrival;

    @JsonbProperty(value = "return", nillable = true)
    private Integer         returnTime;
}
