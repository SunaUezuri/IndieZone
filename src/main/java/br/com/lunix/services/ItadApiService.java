package br.com.lunix.services;

import br.com.lunix.dto.itad.ItadRecords.*;
import br.com.lunix.mapper.ItadMapper;
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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItadApiService {

    private static final Logger log = LoggerFactory.getLogger(ItadApiService.class);
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiBaseUrl;

    @Autowired
    private ItadMapper itadMapper;

    public ItadApiService(@Value("${itad.api.key}") String apiKey,
                          @Value("${itad.api.baseurl}") String apiBaseUrl,
                          RestTemplateBuilder builder) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
        this.restTemplate = builder.build();
    }

    public List<PrecoPlataforma> buscarPrecosParaJogo(String tituloDoJogo) {
        try {
            String gameId = findGameId(tituloDoJogo);
            if (gameId == null) {
                log.warn("Nenhum ID encontrado na API ITAD para o jogo: {}", tituloDoJogo);
                return Collections.emptyList();
            }
            log.info("ID encontrado para '{}': {}", tituloDoJogo, gameId);
            return getPricesForId(gameId);
        } catch (Exception e) {
            log.error("Erro geral ao buscar preços na ITAD para '{}': {}", tituloDoJogo, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String findGameId(String titulo) {
        URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/games/lookup/v1")
                .queryParam("key", apiKey)
                .queryParam("title", titulo)
                .build().toUri();

        try {
            ItadLookupResponseDto response = restTemplate.getForObject(uri, ItadLookupResponseDto.class);
            return Optional.ofNullable(response)
                    .filter(ItadLookupResponseDto::found)
                    .map(ItadLookupResponseDto::game)
                    .map(ItadGameLookupDto::id)
                    .orElse(null);
        } catch (HttpClientErrorException e) {
            log.error("Erro na API ITAD ao buscar ID para '{}': {} {}", titulo, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }

    private List<PrecoPlataforma> getPricesForId(String gameId) {
        URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/games/prices/v3")
                .queryParam("key", apiKey)
                .queryParam("country", "BR")
                .build().toUri();
        try {
            List<String> requestBody = List.of(gameId);

            ItadPriceResultDto[] response = restTemplate.postForObject(uri, requestBody, ItadPriceResultDto[].class);

            // A cadeia Optional agora opera diretamente sobre o array.
            return Optional.ofNullable(response)
                    .filter(r -> r.length > 0)
                    .map(r -> r[0]) // Pegamos o primeiro (e único) resultado do array
                    .map(ItadPriceResultDto::deals)
                    .map(deals -> deals.stream()
                            .map(itadMapper::toPrecoPlataforma)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        } catch (HttpClientErrorException e) {
            log.error("Erro na API ITAD ao buscar preços para o ID '{}': {} {}", gameId, e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erro geral ao extrair resposta para o ID '{}': {}", gameId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
