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
    public record RawgGenreDto(int id, String name, String slug) {}

    // Record que recebe o nome do desenvolvedor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgDeveloperDto(int id, String name, String slug) {}

    // Mapeia o objeto "platform" interno.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgPlatformDto(int id, String name, String slug) {}

    // Mapeia a entrada principal na lista "platforms", que contém o objeto aninhado.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgPlatformEntryDto(@JsonProperty("platform") RawgPlatformDto platform) {}

    // Record final que recebe todos os dados necessários
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgGameDto(
            int id,
            String slug,
            @JsonProperty("name") String name,
            @JsonProperty("released") LocalDate released,
            @JsonProperty("background_image") String backgroundImage,
            @JsonProperty("platforms") List<RawgPlatformEntryDto> platforms,
            @JsonProperty("genres") List<RawgGenreDto> genres,
            @JsonProperty("developers") List<RawgDeveloperDto> developers,
            @JsonProperty("description_raw") String description
    ) {}

    // Record que recebe a lista de jogos encontrados
    public record RawgApiResponseDto(
            int count,
            String next,
            String previous,
            List<RawgGameDto> results
    ) {}
}
