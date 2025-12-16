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

    // Mapeia a imagem do screenshot
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgScreenshotDto(int id, String image) {}

    // Mapeia o clipe de vídeo (Trailer curto)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgClipDto(String clip, String video, String preview) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgMovieDataDto(@JsonProperty("480") String quality480, @JsonProperty("max") String qualityMax) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgMovieResultDto(int id, String name, String preview, RawgMovieDataDto data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgMoviesResponseDto(int count, List<RawgMovieResultDto> results) {}

    // Record final que recebe todos os dados necessários
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgGameDto(
            int id,
            String slug,
            @JsonProperty("name") String name,
            @JsonProperty("released") LocalDate released,
            @JsonProperty("background_image") String backgroundImage,
            @JsonProperty("esrb_rating") RawgEsrbRatingDto esrbRating,
            @JsonProperty("platforms") List<RawgPlatformEntryDto> platforms,
            @JsonProperty("genres") List<RawgGenreDto> genres,
            @JsonProperty("developers") List<RawgDeveloperDto> developers,
            @JsonProperty("description_raw") String description,
            @JsonProperty("short_screenshots") List<RawgScreenshotDto> shortScreenshots,
            @JsonProperty("clip") RawgClipDto clip
    ) {}

    // Record que recebe a lista de jogos encontrados
    public record RawgApiResponseDto(
            int count,
            String next,
            String previous,
            List<RawgGameDto> results
    ) {}

    // Record para mapear a classificação indicativa dos jogos
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgEsrbRatingDto(int id, String name, String slug) {}
}
