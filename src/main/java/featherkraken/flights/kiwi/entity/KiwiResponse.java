package featherkraken.flights.kiwi.entity;

import java.util.List;

import lombok.Data;

@Data
public class KiwiResponse
{

    private List<KiwiFlight> data;
}
