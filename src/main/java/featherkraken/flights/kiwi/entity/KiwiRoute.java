package featherkraken.flights.kiwi.entity;

import java.util.Date;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class KiwiRoute
{

    private String  cityCodeFrom;

    private String  cityCodeTo;

    private String  cityFrom;

    private String  cityTo;

    private String  airline;

    private String  fare_category;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date    local_departure;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date    local_arrival;

    @JsonbProperty(value = "return", nillable = true)
    private Integer returnTime;
}
