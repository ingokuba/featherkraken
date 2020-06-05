package featherkraken.flights.test;

import static lombok.AccessLevel.PRIVATE;

import java.util.Calendar;
import java.util.Date;

import featherkraken.flights.entity.Airport;
import featherkraken.flights.entity.SearchRequest;
import featherkraken.flights.entity.SearchRequest.ClassType;
import featherkraken.flights.entity.SearchRequest.TripType;
import featherkraken.flights.entity.Timespan;
import lombok.NoArgsConstructor;

/**
 * Builder for all entities for testing purposes.
 */
@NoArgsConstructor(access = PRIVATE)
public class EntityBuilder
{

    /**
     * Setup a valid airport with all attributes.
     * 
     * @return Frankfurt FRA
     */
    public static Airport fullAirport()
    {
        return new Airport().setName("FRA").setDisplayName("Frankfurt").setLatitude(50.033056).setLongitude(8.570556);
    }

    /**
     * Setup a minimal airport with given name.
     * 
     * @param name Name of the airport.
     */
    public static Airport airportWithName(String name)
    {
        return new Airport().setName(name);
    }

    /**
     * Setup a valid search request with all mandatory attributes.
     * 
     * @return Search request for a round flight without radius
     */
    public static SearchRequest fullSearchRequest()
    {
        return new SearchRequest()
            .setLimit(1)
            .setTripType(TripType.ROUND_TRIP)
            .setClassType(ClassType.ECONOMY)
            .setPassengers(1)
            .setRadius(0)
            .setSource(airportWithName("FRA"))
            .setTarget(airportWithName("LAX"))
            .setDeparture(new Timespan().setFrom(fromToday(1, Calendar.MONTH)))
            .setReturn(new Timespan().setFrom(fromToday(2, Calendar.MONTH)));
    }

    /**
     * Get date in the future.
     * 
     * @param amount of given unit.
     * @param field unit of the steps.
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/api/java/util/Calendar.html#fields">calendar
     *      fields</a>
     */
    public static Date fromToday(int amount, int field)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        return calendar.getTime();
    }
}
