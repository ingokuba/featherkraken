package featherkraken.flights.kiwi.boundary;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.annotation.JsonFormat;

import featherkraken.flights.kiwi.entity.KiwiResponse;

@Path("/flights")
public interface KiwiApi
{

    @GET
    @Path("/search")
    KiwiResponse getFlights(@HeaderParam("apikey") String apiKey,
                            @QueryParam("curr") String currency, @QueryParam("limit") Integer limit, @QueryParam("max_stopovers") Integer stops,
                            @QueryParam("fly_from") String source, @QueryParam("fly_to") String target, @QueryParam("flight_type") String flightType,
                            @QueryParam("selected_cabins") String classType, @QueryParam("mix_with_cabins") String mixClasses,
                            @QueryParam("date_from") @JsonFormat(pattern = "dd/MM/yyyy") Date dateFrom,
                            @QueryParam("date_to") @JsonFormat(pattern = "dd/MM/yyyy") Date dateTo,
                            @QueryParam("return_from") @JsonFormat(pattern = "dd/MM/yyyy") Date returnFrom,
                            @QueryParam("return_to") @JsonFormat(pattern = "dd/MM/yyyy") Date returnTo);

}
