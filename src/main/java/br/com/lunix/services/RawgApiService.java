package br.com.lunix.services;

import br.com.lunix.dto.rawg.RawgRecords.RawgApiResponseDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgGameDto;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Service
public class RawgApiService {

    private static final Logger log = LoggerFactory.getLogger(RawgApiService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rawg.api.key}")
    private String apiKey;

    @Value("${rawg.api.baseurl}")
    private String apiBaseUrl;

    public List<RawgGameDto> buscarJogos(String termoBusca, int limite) {
        log.info("Buscando jogos na RAWG com o termo '{}'", termoBusca);

        // Construindo a String de conexão com a API
        String url = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/api/games")
                .queryParam("key", apiKey)
                .queryParam("search", termoBusca)
                .queryParam("page_size", limite)
                .toUriString();

        try {
            RawgApiResponseDto response = restTemplate.getForObject(url, RawgApiResponseDto.class);

            if (response != null && response.results() != null) {
                log.info("Encontrados {} resultados para '{}'", response.results().size(), termoBusca);

                return response.results();
            }
        } catch (HttpClientErrorException e) {
            log.error("Erro na chamada para a API da RAWG: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Ocorreu um erro inesperado ao buscar jogos na RAWG", e);
        }

        log.warn("Nenhum resultado encontrado ou erro na API para a busca: '{}'", termoBusca);
        return Collections.emptyList();
    }
}
