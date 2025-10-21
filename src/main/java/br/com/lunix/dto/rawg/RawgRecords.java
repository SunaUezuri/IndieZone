package br.com.lunix.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class RawgRecords {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgGenreDto(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgDeveloperDto(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgGameDto(
            @JsonProperty("name") String name,
            @JsonProperty("description_raw") String description,
            @JsonProperty("released") LocalDate released,
            @JsonProperty("background_image") String backgroundImage,
            @JsonProperty("genres") List<RawgGenreDto> genres,
            @JsonProperty("developers") List<RawgDeveloperDto> developers
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawgApiResponseDto(List<RawgGameDto> results) {}
}
