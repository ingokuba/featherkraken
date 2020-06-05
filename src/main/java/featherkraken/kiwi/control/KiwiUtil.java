package featherkraken.kiwi.control;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = PRIVATE)
public final class KiwiUtil
{

    public static final String TEQUILA_API_KEY = "tequilaApiKey";

    public static String getApiKey()
    {
        String apiKey = System.getProperty(TEQUILA_API_KEY);
        if (apiKey == null) {
            log.severe("Property '" + TEQUILA_API_KEY + "' has to be set for the AirportFinder.");
        }
        return apiKey;
    }
}
