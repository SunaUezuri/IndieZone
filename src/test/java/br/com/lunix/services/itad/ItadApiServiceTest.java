package br.com.lunix.services.itad;

import br.com.lunix.mapper.ItadMapper;
import br.com.lunix.model.entities.PrecoPlataforma;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ItadApiService.class)
@Import(ItadMapper.class)
@TestPropertySource(properties = {
        "itad.api.baseurl=https://api.isthereanydeal.com",
        "itad.api.key=TEST_ITAD_KEY",
        "mongock.enabled=false"
})
public class ItadApiServiceTest {

    @Autowired
    private ItadApiService itadApiService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockServer.reset();
    }

    @Test
    public void deveBuscarPrecosComSucessoSeguindoNovoFluxoDaApi() throws Exception {
        // --- CENÁRIO (ARRANGE) ---
        String termoBusca = "Hades";
        String gameId = "018d937f-33f0-7200-80fc-87f769196c84";

        // ... (Mock da primeira chamada para /games/lookup/v1 permanece o mesmo) ...
        String lookupUrl = UriComponentsBuilder.fromUriString("https://api.isthereanydeal.com")
                .path("/games/lookup/v1")
                .queryParam("key", "TEST_ITAD_KEY").queryParam("title", termoBusca)
                .build().toUri().toString();
        String lookupResponse = """
                { "found": true, "game": { "id": "%s", "slug": "hades", "title": "Hades" } }
                """.formatted(gameId);
        mockServer.expect(requestTo(lookupUrl)).andRespond(withSuccess(lookupResponse, MediaType.APPLICATION_JSON));


        // Preparar a SEGUNDA chamada (POST para /games/prices/v3)
        String pricesUrl = UriComponentsBuilder.fromUriString("https://api.isthereanydeal.com")
                .path("/games/prices/v3")
                .queryParam("key", "TEST_ITAD_KEY").queryParam("country", "BR")
                .build().toUri().toString();

        List<String> expectedRequestBody = List.of(gameId);

        String pricesResponse = """
                [
                    {
                        "id": "%s",
                        "deals": [
                            {
                                "shop": {"id": 61, "name": "Steam"},
                                "price": {"amount": 47.49, "currency": "BRL"},
                                "regular": {"amount": 94.99, "currency": "BRL"},
                                "cut": 50,
                                "url": "url/steam",
                                "drm": [{"id": 61, "name": "Steam"}]
                            }
                        ]
                    }
                ]
                """.formatted(gameId);

        mockServer.expect(requestTo(pricesUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedRequestBody)))
                .andRespond(withSuccess(pricesResponse, MediaType.APPLICATION_JSON));

        // --- AÇÃO (ACT) ---
        List<PrecoPlataforma> resultado = itadApiService.buscarPrecosParaJogo(termoBusca);

        // --- VERIFICAÇÃO (ASSERT) ---
        mockServer.verify();

        assertThat(resultado).hasSize(1);
        PrecoPlataforma precoSteam = resultado.get(0);
        assertThat(precoSteam.getNomeLoja()).isEqualTo("Steam");
        assertThat(precoSteam.getPrecoAtual()).isEqualTo(47.49);
    }
}