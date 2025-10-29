package br.com.lunix.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/*
    Classe que comporta diversos records para utilização
    de recebimento de dados da API RawgIo.
*/
public class RawgRecords {

    // Record que recebe o gênero do jogo
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgGenreDto(String name) {}

    // Record que recebe o nome do desenvolvedor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgDeveloperDto(String name) {}

    // Record final que recebe todos os dados necessários
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgGameDto(
            @JsonProperty("name") String name,
            @JsonProperty("description_raw") String description,
            @JsonProperty("released") LocalDate released,
            @JsonProperty("background_image") String backgroundImage,
            @JsonProperty("genres") List<RawgGenreDto> genres,
            @JsonProperty("developers") List<RawgDeveloperDto> developers
    ) {}

    // Record que recebe a lista de jogos encontrados
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgApiResponseDto(List<RawgGameDto> results) {}
}
