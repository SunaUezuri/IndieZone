package br.com.lunix.services;

import br.com.lunix.dto.itad.ItadRecords.*;
import br.com.lunix.model.entities.PrecoPlataforma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import br.com.lunix.dto.itad.ItadSearchRecords.*;
import br.com.lunix.mapper.ItadMapper;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItadApiService {

    private static final Logger log = LoggerFactory.getLogger(ItadApiService.class);

    @Autowired
    private ItadMapper itadMapper;

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiBaseUrl;

    public ItadApiService(@Value("${itad.api.key}") String apiKey,
                          @Value("${itad.api.baseurl}") String apiBaseUrl,
                          RestTemplateBuilder builder) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
        this.restTemplate = builder.build();
    }

    public List<PrecoPlataforma> buscarPrecosParaJogo(String tituloDoJogo) {
        try {
            String plain = findGamePlain(tituloDoJogo);

            if (plain == null || plain.isEmpty()) {
                log.warn("Nenhum 'plain' encontrado na API ITAD para o jogo: {}", tituloDoJogo);
                return Collections.emptyList();
            }

            log.info("'Plain' encontrado para '{}': '{}'", tituloDoJogo, plain);

            return getPricesForPlain(plain);
        } catch (Exception e) {
            log.error("Erro geral ao buscar preços na ITAD para '{}': {}", tituloDoJogo, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String findGamePlain(String titulo) {
        URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/v02/search/search/")
                .queryParam("key", apiKey)
                .queryParam("q", titulo)
                .queryParam("limit", 1)
                .build()
                .toUri();

        try {
            ItadSearchResponseDto response = restTemplate.getForObject(uri, ItadSearchResponseDto.class);

            return Optional.ofNullable(response)
                    .map(ItadSearchResponseDto::data)
                    .map(ItadSearchDataDto::list)
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0))
                    .map(ItadSearchResultDto::plain)
                    .orElse(null);

        } catch (HttpClientErrorException e) {
            log.error("Erro na API ITAD ao buscar 'plain' para '{}': {} {}", titulo, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }

    private List<PrecoPlataforma> getPricesForPlain(String plain) {
        URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/v01/game/prices/")
                .queryParam("key", apiKey)
                .queryParam("plains", plain)
                .queryParam("country", "BR")
                .build()
                .toUri();

        try {
            ItadPricesResponseDto response = restTemplate.getForObject(uri, ItadPricesResponseDto.class);

            return Optional.ofNullable(response)
                    .map(ItadPricesResponseDto::data)
                    .map(dataMap -> dataMap.get(plain))
                    .map(ItadPriceDataDto::list)
                    .map(priceEntries -> priceEntries.stream()
                            .map(itadMapper::toPrecoPlataforma)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        } catch (HttpClientErrorException e) {
            log.error("Erro na API ITAD ao buscar preços para o 'plain' '{}': {} {}", plain, e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        }
    }
}
