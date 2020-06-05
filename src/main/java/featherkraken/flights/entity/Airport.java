package featherkraken.flights.entity;

import java.util.Objects;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
@Accessors(chain = true)
public class Airport
{

    private String name;

    private String displayName;

    private Double latitude;

    private Double longitude;

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Airport)) {
            return false;
        }
        Airport airport = (Airport)o;
        return name != null && name.equals(airport.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
