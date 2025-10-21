package br.com.lunix.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ItadRecords {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadShopDto(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPriceEntryDto(
            @JsonProperty("shop") ItadShopDto shop,
            @JsonProperty("price_new") double precoAtual,
            @JsonProperty("price_old") double precoBase,
            @JsonProperty("price_cut") int descontoPercentual,
            @JsonProperty("url") String urlLoja
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPriceDataDto(List<ItadPriceEntryDto> list) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPricesResponseDto(Map<String, ItadPriceDataDto> data) {}
}
