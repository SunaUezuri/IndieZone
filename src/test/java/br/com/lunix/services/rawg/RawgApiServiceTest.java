package br.com.lunix.services.rawg;

import br.com.lunix.dto.rawg.RawgRecords.RawgGameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RawgApiService.class)
@TestPropertySource(properties = {
        "rawg.api.baseurl=https://api.rawg.io",
        "rawg.api.key=TEST_API_KEY",
        "mongock.enabled=false"
})
class RawgApiServiceTest {

    @Autowired
    private RawgApiService rawgApiService;

    @Autowired
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        mockServer.reset();
    }

    @Test
    public void deveBuscarJogosERetornarListaDeDTOsComSucesso() throws Exception {
        // Cenário (Arrange)
        String termoBusca = "hollow knight";
        int limite = 1;
        int idJogo = 12345;

        String searchUrl = UriComponentsBuilder.fromUriString("https://api.rawg.io")
                .path("/api/games")
                .queryParam("key", "TEST_API_KEY")
                .queryParam("search", termoBusca)
                .queryParam("page_size", limite)
                .toUriString();

        String jsonGameResponse = """
                {
                    "results": [
                        {
                            "id": 12345,
                            "slug": "hollow-knight",
                            "name": "Hollow Knight",
                            "released": "2017-02-24",
                            "background_image": "https://example.com/hollow_knight.jpg",
                            "esrb_rating": { "id": 2, "name": "Everyone 10+", "slug": "everyone-10-plus" },
                            "platforms": [
                                { "platform": { "id": 4, "name": "PC", "slug": "pc" } }
                            ],
                            "genres": [{"id": 51, "name": "Indie", "slug": "indie"}],
                            "developers": [{"id": 987, "name": "Team Cherry", "slug": "team-cherry"}]
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo(searchUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonGameResponse, MediaType.APPLICATION_JSON));

        String moviesUrl = UriComponentsBuilder.fromUriString("https://api.rawg.io")
                .path("/api/games/" + idJogo + "/movies")
                .queryParam("key", "TEST_API_KEY")
                .toUriString();

        String jsonMoviesResponse = """
                {
                    "count": 1,
                    "results": [
                        {
                            "id": 999,
                            "name": "Trailer Oficial",
                            "preview": "http://img.com/preview.jpg",
                            "data": {
                                "480": "http://video.com/trailer_480.mp4",
                                "max": "http://video.com/trailer_max.mp4"
                            }
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo(moviesUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonMoviesResponse, MediaType.APPLICATION_JSON));


        // --- Ação (Act) ---
        List<RawgGameDto> resultado = rawgApiService.buscarJogos("hollow knight", 1);


        // --- Verificação (Assert) ---
        mockServer.verify(); // Verifica se AS DUAS chamadas foram feitas

        assertThat(resultado).isNotNull().hasSize(1);
        RawgGameDto jogoRetornado = resultado.get(0);

        // Verificações padrão
        assertThat(jogoRetornado.name()).isEqualTo("Hollow Knight");
        assertThat(jogoRetornado.esrbRating().slug()).isEqualTo("everyone-10-plus");
        assertThat(jogoRetornado.clip()).isNotNull();
        assertThat(jogoRetornado.clip().clip()).isEqualTo("http://video.com/trailer_max.mp4");
    }
}