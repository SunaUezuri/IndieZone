package br.com.lunix.dto.igdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Classe que armazena os DTOS utilizados na chamada da api do IGDB
public class IgdbRecords {

    // DTO de resposta para a autenticação
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TwitchAuthResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") Long expiresIn) {}

    // DTO que pega o nome da empresa e a logo
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IgdbCompanyDto(String name, IgdbLogoDto logo) {}

    // Dto para pegar a url da imagem
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IgdbLogoDto(String url) {}
}
