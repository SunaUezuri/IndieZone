package br.com.lunix.services;

import br.com.lunix.mapper.ItadMapper;
import br.com.lunix.model.entities.PrecoPlataforma;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
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

    @BeforeEach
    void setup() {
        mockServer.reset();
    }

    @Test
    public void deveBuscarPrecosComSucessoQuandoAmbasAsChamadasFuncionam() {
        String termoBusca = "Hades";
        String plain = "hades";

        // Preparando a primeira chamada (busca pelo 'plain')
        String searchUrl = UriComponentsBuilder.fromUriString("https://api.isthereanydeal.com")
                .path("/v02/search/search/")
                .queryParam("key", "TEST_ITAD_KEY").queryParam("q", termoBusca).queryParam("limit", 1)
                .build().toUri().toString();

        String searchResponse = """
                { "data": { "list": [{ "plain": "hades", "title": "Hades" }] } }
                """;

        mockServer.expect(requestTo(searchUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));

        // 2. Preparar a SEGUNDA chamada (busca pelos preços)
        String pricesUrl = UriComponentsBuilder.fromUriString("https://api.isthereanydeal.com")
                .path("/v01/game/prices/")
                .queryParam("key", "TEST_ITAD_KEY").queryParam("plains", plain).queryParam("country", "BR")
                .build().toUri().toString();

        String pricesResponse = """
                { "data": { "hades": { "list": [
                    { "shop": {"name": "Steam"}, "price_new": 47.49, "price_old": 94.99, "price_cut": 50, "url": "url/steam" },
                    { "shop": {"name": "GOG"}, "price_new": 94.99, "price_old": 94.99, "price_cut": 0, "url": "url/gog" }
                ]}}}
                """;

        mockServer.expect(requestTo(pricesUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(pricesResponse, MediaType.APPLICATION_JSON));

        List<PrecoPlataforma> resultado = itadApiService.buscarPrecosParaJogo(termoBusca);

        mockServer.verify(); // Garante que AMBAS as chamadas esperadas aconteceram.

        assertThat(resultado).hasSize(2);
        PrecoPlataforma precoSteam = resultado.get(0);
        assertThat(precoSteam.getNomeLoja()).isEqualTo("Steam");
        assertThat(precoSteam.getPrecoAtual()).isEqualTo(47.49);
    }

    @Test
    public void deveRetornarListaVaziaQuandoPlainNaoEhEncontrado() {
        String termoBusca = "Jogo Inexistente";
        String searchUrl = UriComponentsBuilder.fromUriString("https://api.isthereanydeal.com")
                .path("/v02/search/search/")
                .queryParam("key", "TEST_ITAD_KEY").queryParam("q", termoBusca).queryParam("limit", 1)
                .build().toUri().toString();

        String searchResponse = """
                { "data": { "list": [] } }
                """;

        mockServer.expect(requestTo(searchUrl))
                .andRespond(withSuccess(searchResponse, MediaType.APPLICATION_JSON));

        List<PrecoPlataforma> resultado = itadApiService.buscarPrecosParaJogo(termoBusca);

        mockServer.verify();
        assertThat(resultado).isEmpty();
    }
}
