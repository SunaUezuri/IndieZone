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

/*
    Serviço utilizado para buscar preços de um jogo em diversas plataformas.

    Busca a partir do título, de onde é recuperado um id que será
    utilizado para as lojas e preços referentes ao jogo.
*/
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

    /*
        Método principal que utiliza os outros dois para realizar a pesquisa dos jogos.

        @param tituloDoJogo: Titulo do jogo a ser buscado.

        @return: Retorna uma lista com todas as lojas e seus preços
        do jogo buscado. Em caso de erro devolve uma lista vazia.
    */
    public List<PrecoPlataforma> buscarPrecosParaJogo(String tituloDoJogo) {
        /*
            Primeiro chama o método findGameId para encontrar
            o identificador único do jogo na API.

            Após chama o método getPricesForId com o ID encontrado
            e devolve a lisca de precos.

            Em caso de erros ele sempre devolverá uma lista vazia.
        */

        try {
            // Chamada do método para encontrar o ID
            String gameId = findGameId(tituloDoJogo);

            // Verificação para saber se o ID foi encontrado
            if (gameId == null) {
                log.warn("Nenhum ID encontrado na API ITAD para o jogo: {}", tituloDoJogo);
                return Collections.emptyList();
            }
            log.info("ID encontrado para '{}': {}", tituloDoJogo, gameId);

            // Chama o método que gera a lista de lojas a partir do ID
            return getPricesForId(gameId);
        } catch (Exception e) {
            log.error("Erro geral ao buscar preços na ITAD para '{}': {}", tituloDoJogo, e.getMessage());
            return Collections.emptyList();
        }
    }

    /*
        Método que busca o ID de um jogo da API pelo título.

        @param titulo: Título do jogo a ser buscado.
        @return: Retorna um ID no formato de String.
    */
    private String findGameId(String titulo) {
        /*
            Utiliza o padrão de criação de Link com URI para se conectar
            com a API, então ele salva a resposta dada em um objeto
            ItadLookupResponseDto e filtra a resposta registrando o jogo
            e seu id respectivo.

            Caso não seja encontrado ele devolverá um null.
        */

        // Formatação para a chamada da API
        URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/games/lookup/v1")
                .queryParam("key", apiKey)
                .queryParam("title", titulo)
                .build().toUri();

        try {
            // Registra a resposta
            ItadLookupResponseDto response = restTemplate.getForObject(uri, ItadLookupResponseDto.class);

            // Faz a filtragem da resposta para transformar em DTO
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

    /*
        Método responsável por buscar todos os preços de um jogo
        em diversas lojas a partir de um ID.

        @param gameId: ID do jogo escolhido vindo da API.

        @return: Devolve uma lista de PrecoPlataforma que contém as lojas
        e o preço referente aquela loja.
    */
    private List<PrecoPlataforma> getPricesForId(String gameId) {
        /*
            Faz uma requisição POST para a API enviando o ID por meio de
            URI, depois faz um mapeamento da resposta gerada para transformar
            a resposta em PrecoPlataforma.

            Em caso de erro devolve uma lista nula.
        */

        // Gera o link da requisição.
        URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                .path("/games/prices/v3")
                .queryParam("key", apiKey)
                .queryParam("country", "BR")
                .build().toUri();
        try {
            // Faz o corpo da requisição já que ele só aceita listas
            List<String> requestBody = List.of(gameId);

            // Faz a requisição e armazena no objeto ItadPriceResultDto
            ItadPriceResultDto[] response = restTemplate.postForObject(uri, requestBody, ItadPriceResultDto[].class);

            // Gera uma resposta com Optional para tratar nulos
            return Optional.ofNullable(response)
                    .filter(r -> r.length > 0)
                    .map(r -> r[0]) // Pegamos o primeiro (e único) resultado do array
                    .map(ItadPriceResultDto::deals) // Faz o mapeamento das lojas
                    .map(deals -> deals.stream()
                            .map(itadMapper::toPrecoPlataforma) // Transforma o objeto da resposta em PrecoPlataforma
                            .collect(Collectors.toList())) // Transforma a resposta em uma lista
                    .orElse(Collections.emptyList()); // Em caso de falha devolve uma lista nula

        } catch (HttpClientErrorException e) {
            log.error("Erro na API ITAD ao buscar preços para o ID '{}': {} {}", gameId, e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erro geral ao extrair resposta para o ID '{}': {}", gameId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
