package br.com.lunix.services.igdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import br.com.lunix.dto.igdb.IgdbRecords.TwitchAuthResponse;
import br.com.lunix.dto.igdb.IgdbRecords.IgdbCompanyDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@Slf4j
public class IgdbApiService {

    private final RestTemplate restTemplate;

    @Value("${twitch.auth-server.url}")
    private String twitchAuthUrl;

    @Value("${twitch.client-id}")
    private String clientId;

    @Value("${twitch.client-secret}")
    private String clientSecret;

    @Value("${igdb.api.url}")
    private String igdbApiUrl;

    // Implementação de cache simples em memória
    private String accessToken;
    private LocalDateTime tokenExpiration;

    public IgdbApiService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    /*
        Busca o logo de uma empresa pelo nome.
        Retorna null se não encontrar para não quebrar o cadastro.

        @param nomeEmpresa - Nome da empresa a ter a logo buscada
    */
    public String buscarLogoEmpresa(String nomeEmpresa) {
        log.info("IGDB: Iniciando busca de logo para '{}'", nomeEmpresa);
        try {
            ensureTokenIsValid();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Client-ID", clientId);
            headers.add("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.TEXT_PLAIN);

            // Query de pesquisa da API para encontrar uma logo com o exato nome recebido
            String body = String.format(
                    "fields name, logo.url; where name ~ \"%s\" & logo != null; limit 1;",
                    nomeEmpresa
            );

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            URI uri = UriComponentsBuilder.fromUriString(igdbApiUrl)
                    .path("/companies")
                    .build()
                    .toUri();

            IgdbCompanyDto[] response = restTemplate.postForObject(
                    uri,
                    request,
                    IgdbCompanyDto[].class
            );

            log.info("IGDB Resposta RAW: {}", Arrays.toString(response));

            if (response != null && response.length > 0 && response[0].logo() != null) {
                String url = response[0].logo().url();
                if (url.startsWith("//")) {
                    url = "https:" + url;
                }
                String finalUrl = url.replace("t_thumb", "t_logo_med");
                log.info("IGDB: Logo encontrado: {}", finalUrl);
                return finalUrl;
            } else {
                log.warn("IGDB: Nenhum logo encontrado para '{}'. Verifique se o nome está correto na IGDB.", nomeEmpresa);
            }

        } catch (Exception e) {
            log.error("IGDB: Erro ao buscar logo para a empresa: {}", nomeEmpresa, e);
        }
        // Caso não encontre uma logo o cadastro não falha, ele apenas devolve null
        return null;
    }

    /*
        Lógica para a autenticação da API
    */
    private void ensureTokenIsValid() {
        if (accessToken == null || LocalDateTime.now().isAfter(tokenExpiration)) {
            log.info("Gerando novo token de acesso na Twitch...");

            URI uri = UriComponentsBuilder.fromUriString(twitchAuthUrl)
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("grant_type", "client_credentials")
                    .build()
                    .toUri();

            TwitchAuthResponse response = restTemplate.postForObject(uri, null, TwitchAuthResponse.class);

            if (response != null) {
                this.accessToken = response.accessToken();
                this.tokenExpiration = LocalDateTime.now().plusSeconds(response.expiresIn() - 300);
            } else {
                throw new RuntimeException("Falha ao autenticar na Twitch API");
            }
        }
    }
}
