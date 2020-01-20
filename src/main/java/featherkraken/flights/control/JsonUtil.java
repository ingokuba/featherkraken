package featherkraken.flights.control;

import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class JsonUtil
{

    /**
     * Transform {@link JsonArray} to List of Strings.
     */
    public static List<String> toStringList(JsonArray array)
    {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }
}
