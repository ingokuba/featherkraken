package featherkraken.airports.control;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import featherkraken.flights.entity.Airport;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = PRIVATE)
public class AirportFinder
{

    public static final String  API_KEY  = "apiKey";

    private static final String ENDPOINT = "https://cometari-airportsfinder-v1.p.rapidapi.com/api/airports/by-radius";

    /**
     * Find airports in a specific radius.
     */
    public static List<Airport> findAirports(Airport source, Integer radius)
    {
        List<Airport> airports = new ArrayList<>();
        if (radius == null || radius <= 0) {
            return Arrays.asList(source);
        }
        String apiKey = System.getProperty(API_KEY);
        if (apiKey == null) {
            log.severe("Property '" + API_KEY + "' has to be set for the AirportFinder.");
            return Arrays.asList(source);
        }
        try {
            Response response = ClientBuilder.newClient().target(ENDPOINT)
                .queryParam("radius", radius)
                .queryParam("lat", source.getLatitude())
                .queryParam("lng", source.getLongitude())
                .request(APPLICATION_JSON_TYPE)
                .header("x-rapidapi-host", "cometari-airportsfinder-v1.p.rapidapi.com")
                .header("x-rapidapi-key", apiKey)
                .get();
            JsonArray jsonAirports = response.readEntity(JsonArray.class);
            jsonAirports.forEach(jsonAirport -> airports.add(parseAirport((JsonObject)jsonAirport)));
            String concatAirports = airports.stream().map(Airport::getName).collect(joining(", "));
            log.info(format("Found %1$d possible airports: %2$s", airports.size(), concatAirports));
            return airports;
        } catch (Exception e) {
            log.log(Level.WARNING, "AirportFinder responded with error.", e);
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
            .setLatitude(location.getJsonNumber("latitude").doubleValue())
            .setLongitude(location.getJsonNumber("longitude").doubleValue());
    }
}
