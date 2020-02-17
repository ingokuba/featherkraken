package featherkraken.flights.error;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Wrapper for all errors returned in a response.
 * 
 * <pre>
 * {
 * "errors": [
 * ...
 * ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse
{

    @Singular
    private List<Error> errors;

}