package br.com.lunix.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ItadRecords {

    // --- DTOs para o endpoint /games/lookup/v1 ---
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadGameLookupDto(String id, String slug, String title) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadLookupResponseDto(boolean found, ItadGameLookupDto game) {}

    /** Mapeia os objetos aninhados de preço ("price" e "regular"). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPriceDetailsDto(double amount, String currency) {}

    /** Mapeia o objeto "shop" que contém o ID e o nome da loja. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadShopDto(int id, String name) {}

    /** Mapeia uma "deal" (oferta) individual. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadDealDto(
            @JsonProperty("shop") ItadShopDto shop,
            @JsonProperty("price") ItadPriceDetailsDto price,
            @JsonProperty("regular") ItadPriceDetailsDto regular,
            @JsonProperty("cut") int priceCut,
            @JsonProperty("url") String url,
            @JsonProperty("drm") List<ItadDrmDto> drm
    ) {}

    /** Mapeia o objeto de preço de um único jogo, que contém seu ID e a lista de deals. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPriceResultDto(
            String id,
            @JsonProperty("deals") List<ItadDealDto> deals
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadDrmDto(
            int id,
            String name
    ) {}
}