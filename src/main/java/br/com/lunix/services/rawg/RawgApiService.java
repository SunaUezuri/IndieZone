package br.com.lunix.services.rawg;

import br.com.lunix.dto.rawg.RawgRecords.RawgApiResponseDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgGameDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgGenreDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgMoviesResponseDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgClipDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgMovieResultDto;
import br.com.lunix.exceptions.JogoNaoIndieException;
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
import java.util.stream.Collectors;


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

                // Fluxo: Filtra -> Enriquece com Trailer -> Coleta
                List<RawgGameDto> jogosProcessados = response.results().stream()
                        .filter(this::isIndie)
                        .map(this::enriquecerComTrailerSeNecessario)
                        .collect(Collectors.toList());

                if (jogosProcessados.isEmpty()) {
                    log.warn("O jogo '{}' foi encontrado na RAWG, mas filtrado pois não é INDIE.", termoBusca);
                    throw new JogoNaoIndieException("O jogo não é classificado como INDIE.");
                }

                return jogosProcessados;
            }
        } catch (JogoNaoIndieException e){
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("Erro na chamada para a API da RAWG: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Ocorreu um erro inesperado ao buscar jogos na RAWG", e);
        }

        // Caso não encontre jogos devolve um aviso e uma lista vazia
        log.warn("Nenhum resultado encontrado ou erro na API para a busca: '{}'", termoBusca);
        return Collections.emptyList();
    }

    /*
      Verifica se o jogo tem o campo 'clip' preenchido.
      Se não tiver, faz uma chamada extra ao endpoint de movies para tentar achar um trailer.

      @param jogo - Jogo encontrado
    */
    private RawgGameDto enriquecerComTrailerSeNecessario(RawgGameDto jogo) {
        // Se já tem clipe vindo da busca principal, não faz nada
        if (jogo.clip() != null && jogo.clip().clip() != null) {
            return jogo;
        }

        log.info("RAWG: Trailer não encontrado no objeto principal para '{}'. Buscando em /movies...", jogo.name());
        String trailerUrl = buscarTrailerUrl(jogo.id());

        if (trailerUrl != null) {
            log.info("RAWG: Trailer encontrado externamente: {}", trailerUrl);

            // Cria um objeto Clip artificial com a URL encontrada
            RawgClipDto novoClip = new RawgClipDto(trailerUrl, trailerUrl, null);

            return new RawgGameDto(
                    jogo.id(),
                    jogo.slug(),
                    jogo.name(),
                    jogo.released(),
                    jogo.backgroundImage(),
                    jogo.esrbRating(),
                    jogo.platforms(),
                    jogo.genres(),
                    jogo.developers(),
                    jogo.description(),
                    jogo.shortScreenshots(),
                    novoClip
            );
        }

        return jogo; // Se não achou nada, retorna o original
    }

    /*
       Método auxiliar para buscar o trailer do jogo com lógica de prioridade
       @param jogoId - Identificador do jogo
    */
    private String buscarTrailerUrl(int jogoId) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(apiBaseUrl)
                    .path("/api/games/" + jogoId + "/movies")
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            RawgMoviesResponseDto response = restTemplate.getForObject(uri, RawgMoviesResponseDto.class);

            if (response != null && response.results() != null && !response.results().isEmpty()) {
                List<RawgMovieResultDto> videos = response.results();

                String url = buscarPorPalavrasChave(videos, "trailer", "launch", "official");
                if (url != null) return url;

                url = buscarPorPalavrasChave(videos, "teaser", "reveal", "announcement");
                if (url != null) return url;

                url = buscarPorPalavrasChave(videos, "gameplay", "demo", "first look");
                if (url != null) return url;

                // Fallback: Se não casar com nada, pega o primeiro da lista
                log.info("RAWG: Nenhuma palavra-chave encontrada. Usando o primeiro vídeo disponível.");
                return extrairUrlVideo(videos.get(0));
            }
        } catch (Exception e) {
            log.warn("RAWG: Falha ao buscar trailer extra para o ID {}: {}", jogoId, e.getMessage());
        }
        return null;
    }

    /*
        Método auxiliar para extrair a URL de melhor qualidade
    */
    private String extrairUrlVideo(RawgMovieResultDto movie) {
        if (movie.data() != null) {
            if (movie.data().qualityMax() != null) return movie.data().qualityMax();
            if (movie.data().quality480() != null) return movie.data().quality480();
        }
        return null;
    }

    /*
        Método auxiliar que itera sobre a lista procurando qualquer uma das palavras fornecidas
    */
    private String buscarPorPalavrasChave(List<RawgMovieResultDto> videos, String... palavras) {
        for (RawgMovieResultDto movie : videos) {
            String videoName = (movie.name() != null) ? movie.name().toLowerCase() : "";

            for (String palavra : palavras) {
                if (videoName.contains(palavra)) {
                    String url = extrairUrlVideo(movie);
                    if (url != null) {
                        log.info("RAWG: Vídeo encontrado pela palavra-chave '{}': {}", palavra, movie.name());
                        return url;
                    }
                }
            }
        }
        return null;
    }

    /*
        Método auxiliar para verificar se o jogo puxado da API é Indie

        @param game: Jogo específico que é pesquisado
        @return: retorna true se o jogo for indie, se for false a api deve retornar nada
    */
    private boolean isIndie(RawgGameDto game) {
        if (game.genres() == null || game.genres().isEmpty()){
            return false;
        }

        for (RawgGenreDto genre : game.genres()) {
            if ("indie".equalsIgnoreCase(genre.slug()) || "indie".equalsIgnoreCase(genre.name())) {
                return true;
            }
        }
        return false;
    }
}
