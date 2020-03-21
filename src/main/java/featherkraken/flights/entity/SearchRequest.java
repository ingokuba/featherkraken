package featherkraken.flights.entity;

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

    private Timespan  departure;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonProperty("return")
    private Timespan  returnDate;

    private Integer   limit;

    private Integer   stops;

    public SearchRequest setDeparture(Timespan departure)
    {
        if (departure.getTo() == null) {
            departure.setTo(departure.getFrom());
        }
        this.departure = departure;
        return this;
    }

    public Timespan getReturn()
    {
        return returnDate;
    }

    public SearchRequest setReturn(Timespan returnDate)
    {
        if (returnDate.getTo() == null) {
            returnDate.setTo(returnDate.getFrom());
        }
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
