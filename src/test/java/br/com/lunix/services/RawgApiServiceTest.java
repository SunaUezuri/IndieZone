package br.com.lunix.services;

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
    void deveBuscarJogosERetornarListaDeDTOsComSucesso() throws Exception {
        // Cenário (Arrange)
        String termoBusca = "hollow knight";
        int limite = 1;

        // 1. Construímos a URL EXATA que esperamos que nosso serviço chame.
        String expectedUrl = UriComponentsBuilder.fromUriString("https://api.rawg.io")
                .path("/api/games")
                .queryParam("key", "TEST_API_KEY")
                .queryParam("search", termoBusca)
                .queryParam("page_size", limite)
                .toUriString();

        // 2. Criamos o corpo da resposta JSON que nosso servidor FALSO irá devolver.
        String jsonResponse = """
                {
                    "count": 1,
                    "results": [
                        {
                            "name": "Hollow Knight",
                            "description_raw": "Explore a vast ruined kingdom...",
                            "released": "2017-02-24",
                            "background_image": "https://example.com/hollow_knight.jpg",
                            "genres": [{"name": "Metroidvania"}],
                            "developers": [{"name": "Team Cherry"}]
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<RawgGameDto> resultado = rawgApiService.buscarJogos(termoBusca, limite);

        mockServer.verify(); // Garante que a chamada esperada realmente aconteceu.

        assertThat(resultado).isNotNull().hasSize(1);
        RawgGameDto jogoRetornado = resultado.get(0);
        assertThat(jogoRetornado.name()).isEqualTo("Hollow Knight");
        assertThat(jogoRetornado.backgroundImage()).isEqualTo("https://example.com/hollow_knight.jpg");
    }
}