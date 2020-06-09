package featherkraken.flights.kiwi.entity;

import javax.json.bind.annotation.JsonbProperty;

import lombok.Data;

@Data
public class KiwiDuration
{

    private Integer departure;

    @JsonbProperty(value = "return", nillable = true)
    private Integer returnTime;
}
