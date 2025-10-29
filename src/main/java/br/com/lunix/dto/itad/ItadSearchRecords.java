package br.com.lunix.dto.itad;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class ItadSearchRecords {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadSearchResultDto(
            String plain,
            String title
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadSearchDataDto(
            List<ItadSearchResultDto> list
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItadSearchResponseDto(
            ItadSearchDataDto data
    ) {}
}
