package featherkraken.airports.kiwi.control;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import featherkraken.flights.entity.Airport;
import featherkraken.kiwi.control.KiwiUtil;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

/**
 * Find airport by query string.
 */
@Log
@NoArgsConstructor(access = PRIVATE)
public class KiwiAirportLookup
{

    private static final String ENDPOINT = "https://tequila-api.kiwi.com/locations/query";

    /**
     * Find airports with the given query string.
     * Searches in name, city and IATA code.
     */
    public static List<Airport> executeSearch(String query)
    {
        List<Airport> airports = new ArrayList<>();
        String apiKey = KiwiUtil.getApiKey();
        if (apiKey == null) {
            return airports;
        }
        Response response = ClientBuilder.newClient().target(ENDPOINT)
            .queryParam("term", query)
            .queryParam("location_types", "airport")
            .request(APPLICATION_JSON_TYPE)
            .header("apikey", apiKey).get();
        StatusType status = response.getStatusInfo();
        if (!OK.equals(status.toEnum())) {
            log.severe(format("Response code was: %1$d %2$s", status.getStatusCode(), status.getReasonPhrase()));
            return airports;
        }
        JsonObject json = response.readEntity(JsonObject.class);
        JsonValue data = json.get("locations");
        if (data == null) {
            return airports;
        }
        JsonArray jsonLocations = (JsonArray)data;
        jsonLocations.forEach(location -> airports.add(parseAirport((JsonObject)location)));
        return airports;
    }

    private static Airport parseAirport(JsonObject location)
    {
        JsonObject latLon = location.getJsonObject("location");
        return new Airport()
            .setName(location.getString("code"))
            .setDisplayName(location.getString("name"))
            .setLatitude(latLon.getJsonNumber("lat").doubleValue())
            .setLongitude(latLon.getJsonNumber("lon").doubleValue());
    }
}
