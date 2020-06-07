package featherkraken.airports.kiwi.control;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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
 * Find airport in a given radius.
 */
@Log
@NoArgsConstructor(access = PRIVATE)
public class KiwiAirportFinder
{

    private static final String ENDPOINT = "https://tequila-api.kiwi.com/locations/radius";

    /**
     * Find airports in a specific radius.
     */
    public static List<Airport> findAirports(Airport source, Integer radius)
    {
        List<Airport> airports = new ArrayList<>();
        if (radius == null || radius <= 0) {
            return Arrays.asList(source);
        }
        String apiKey = KiwiUtil.getApiKey();
        if (apiKey == null) {
            return Arrays.asList(source);
        }
        try {
            Response response = ClientBuilder.newClient().target(ENDPOINT)
                .queryParam("radius", radius)
                .queryParam("lat", source.getLatitude())
                .queryParam("lon", source.getLongitude())
                .queryParam("location_types", "airport")
                .queryParam("limit", 1337)
                .request(APPLICATION_JSON_TYPE)
                .header("apikey", apiKey)
                .get();
            JsonObject json = response.readEntity(JsonObject.class);
            StatusType status = response.getStatusInfo();
            if (OK.getStatusCode() != status.getStatusCode()) {
                log.severe(format("Response code was: %1$d %2$s", status.getStatusCode(), status.getReasonPhrase()));
                return Arrays.asList(source);
            }
            JsonValue data = json.get("locations");
            if (data == null) {
                return Arrays.asList(source);
            }
            JsonArray jsonAirports = (JsonArray)data;
            jsonAirports.forEach(jsonAirport -> airports.add(parseAirport((JsonObject)jsonAirport)));
            String concatAirports = airports.stream().map(Airport::getName).collect(joining(", "));
            log.info(format("Found %1$d possible airports: %2$s", airports.size(), concatAirports));
            return airports;
        } catch (Exception e) {
            log.log(Level.WARNING, KiwiAirportFinder.class.getSimpleName() + " responded with error.", e);
            return Arrays.asList(source);
        }
    }

    /**
     * Parse json airport to {@link Airport} object.
     */
    private static Airport parseAirport(JsonObject jsonAirport)
    {
        JsonObject location = jsonAirport.getJsonObject("location");
        return new Airport()
            .setName(jsonAirport.getString("code"))
            .setDisplayName(jsonAirport.getString("name"))
            .setLatitude(location.getJsonNumber("lat").doubleValue())
            .setLongitude(location.getJsonNumber("lon").doubleValue());
    }
}
