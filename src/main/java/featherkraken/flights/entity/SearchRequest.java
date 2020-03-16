package featherkraken.flights.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class SearchRequest
{

    private TripType  tripType;

    private ClassType classType;

    private Integer   passengers;

    private Airport   source;

    private Integer   radius;

    private Airport   target;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date      departure;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("return")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date      returnDate;

    private Integer   limit;

    public Date getReturn()
    {
        return returnDate;
    }

    public SearchRequest setReturn(Date returnDate)
    {
        this.returnDate = returnDate;
        return this;
    }

    public enum TripType
    {

        /**
         * Back and forth.
         */
        ROUND_TRIP("Round trip"),
        /**
         * Only one direction.
         */
        ONE_WAY("One-way");

        private final String value;

        TripType(final String name)
        {
            this.value = name;
        }

        @JsonValue
        public String getValue()
        {
            return value;
        }
    }

    public enum ClassType
    {

        ECONOMY("Economy"),
        PREMIUM_ECONOMY("Premium Economy"),
        BUSINESS("Business"),
        FIRST_CLASS("First class");

        private final String value;

        ClassType(final String name)
        {
            this.value = name;
        }

        @JsonValue
        public String getValue()
        {
            return value;
        }
    }
}
