package br.com.lunix.services;

import br.com.lunix.dto.rawg.RawgRecords.RawgApiResponseDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgGameDto;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;


/*
*   Serviço para buscar jogos utilizando a API do Rawg.Io
*
*   Busca jogos a partir do título do mesmo e devolve todas as informações necessárias,
*   é utilizado para facilitar a inserção de jogos na plataforma por meio do formulário
*/
@Service
public class RawgApiService {

    // Logger para a aplicação criar logs personalizados para o serviço, permitindo melhor depuração
    private static final Logger log = LoggerFactory.getLogger(RawgApiService.class);

    private RestTemplate restTemplate;
    private final String apiKey;
    private final String apiBaseUrl;

    // Construtor já passa o valor que o parâmetro vai utilizar
    public RawgApiService(@Value("${rawg.api.key}") String apiKey,
                          @Value("${rawg.api.baseurl}") String apiBaseUrl,
                          RestTemplateBuilder builder) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
        this.restTemplate = builder.build();
    }

    /*
    *   Método principal para buscar os jogos no endpoint search
    *
    *   @param termoBuca - Título do jogo a ser procurado
    *   @param limite - Quantidade de jogos limite que deve encontrar
    */
    public List<RawgGameDto> buscarJogos(String termoBusca, int limite) {
        log.info("Buscando jogos na RAWG com o termo '{}'", termoBusca);

        // Construindo a String de conexão com a API
        URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl) // Utilizando com uri por ser uma forma segura de se criar a url
                .path("/api/games")
                .queryParam("key", apiKey)
                .queryParam("search", termoBusca)
                .queryParam("page_size", limite)
                .build()
                .toUri();

        try {
            // Pega a resposta encontrada e transforma no DTO de resposta da Rawg
            RawgApiResponseDto response = restTemplate.getForObject(uri, RawgApiResponseDto.class);

            if (response != null && response.results() != null) {
                log.info("Encontrados {} resultados para '{}'", response.results().size(), termoBusca);

                return response.results();
            }
        } catch (HttpClientErrorException e) {
            log.error("Erro na chamada para a API da RAWG: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Ocorreu um erro inesperado ao buscar jogos na RAWG", e);
        }

        // Caso não encontre jogos devolve um aviso e uma lista vazia
        log.warn("Nenhum resultado encontrado ou erro na API para a busca: '{}'", termoBusca);
        return Collections.emptyList();
    }
}
