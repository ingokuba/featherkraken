package featherkraken.flights.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Airport
{

    private String name;

    private String displayName;

    private Double latitude;

    private Double longitude;
}
