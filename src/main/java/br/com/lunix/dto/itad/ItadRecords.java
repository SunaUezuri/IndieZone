package br.com.lunix.dto.itad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/*
    Classe que abriga diversos Records que são responsáveis
    por receber dados da API ITAD para receber dados de
    precificação de um jogo.
*/
public class ItadRecords {

    // Record que pega uma loja pelo nome
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadShopDto(String name) {}

    // Record final que pega todos os dados de entrada necessários
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPriceEntryDto(
            @JsonProperty("shop") ItadShopDto shop,
            @JsonProperty("price_new") double precoAtual,
            @JsonProperty("price_old") double precoBase,
            @JsonProperty("price_cut") int descontoPercentual,
            @JsonProperty("url") String urlLoja
    ) {}

    // Record que recebe uma lista com todos os dados
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPriceDataDto(List<ItadPriceEntryDto> list) {}

    // Record que mapeia os dados para uma String
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadPricesResponseDto(Map<String, ItadPriceDataDto> data) {}
}
