package br.com.lunix.services.igdb;

import br.com.lunix.dto.igdb.IgdbRecords.IgdbCompanyDto;
import br.com.lunix.dto.igdb.IgdbRecords.IgdbLogoDto;
import br.com.lunix.dto.igdb.IgdbRecords.TwitchAuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IgdbApiServiceTest {

    private IgdbApiService service;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    // Constantes para simular o application.properties
    private final String TWITCH_URL = "https://id.twitch.tv/oauth2/token";
    private final String IGDB_URL = "https://api.igdb.com/v4";
    private final String CLIENT_ID = "test-id";
    private final String CLIENT_SECRET = "test-secret";

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        service = new IgdbApiService(restTemplateBuilder);

        ReflectionTestUtils.setField(service, "twitchAuthUrl", TWITCH_URL);
        ReflectionTestUtils.setField(service, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(service, "clientSecret", CLIENT_SECRET);
        ReflectionTestUtils.setField(service, "igdbApiUrl", IGDB_URL);
    }

    @Test
    @DisplayName("Cenário Frio: Deve autenticar na Twitch e depois buscar logo no IGDB")
    void buscarLogoEmpresa_ColdStart_Sucesso() {
        TwitchAuthResponse authResponse = new TwitchAuthResponse("token-acesso-123", 3600L);

        // CORREÇÃO: Adicionado uri != null para evitar NPE no matcher
        when(restTemplate.postForObject(
                argThat((URI uri) -> uri != null && uri.toString().contains("twitch.tv")),
                isNull(),
                eq(TwitchAuthResponse.class))
        ).thenReturn(authResponse);

        String urlCrua = "//images.igdb.com/igdb/image/upload/t_thumb/logo.png";
        IgdbLogoDto logoDto = new IgdbLogoDto(urlCrua);
        IgdbCompanyDto companyDto = new IgdbCompanyDto("Team Cherry", logoDto);
        IgdbCompanyDto[] arrayResponse = new IgdbCompanyDto[]{companyDto};

        when(restTemplate.postForObject(
                eq(URI.create(IGDB_URL + "/companies")),
                any(HttpEntity.class),
                eq(IgdbCompanyDto[].class))
        ).thenReturn(arrayResponse);

        String resultado = service.buscarLogoEmpresa("Team Cherry");

        assertThat(resultado).isEqualTo("https://images.igdb.com/igdb/image/upload/t_logo_med/logo.png");

        // CORREÇÃO TAMBÉM NO VERIFY
        verify(restTemplate, times(1)).postForObject(
                argThat((URI uri) -> uri != null && uri.toString().contains("twitch.tv")),
                isNull(),
                eq(TwitchAuthResponse.class));

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(URI.create(IGDB_URL + "/companies")), captor.capture(), eq(IgdbCompanyDto[].class));

        HttpEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getHeaders().getFirst("Authorization")).isEqualTo("Bearer token-acesso-123");
    }

    @Test
    @DisplayName("Cenário Quente: Deve usar token em cache e não chamar Twitch")
    void buscarLogoEmpresa_WarmStart_Sucesso() {
        ReflectionTestUtils.setField(service, "accessToken", "token-existente");
        ReflectionTestUtils.setField(service, "tokenExpiration", LocalDateTime.now().plusHours(1));

        IgdbLogoDto logoDto = new IgdbLogoDto("//url.com/img.jpg");
        IgdbCompanyDto companyDto = new IgdbCompanyDto("Nintendo", logoDto);
        IgdbCompanyDto[] arrayResponse = new IgdbCompanyDto[]{companyDto};

        when(restTemplate.postForObject(
                eq(URI.create(IGDB_URL + "/companies")),
                any(HttpEntity.class),
                eq(IgdbCompanyDto[].class))
        ).thenReturn(arrayResponse);

        service.buscarLogoEmpresa("Nintendo");

        // CORREÇÃO NO VERIFY
        verify(restTemplate, never()).postForObject(
                argThat((URI uri) -> uri != null && uri.toString().contains("twitch.tv")),
                any(),
                any());
    }

    @Test
    @DisplayName("Deve retornar null se a empresa não for encontrada (Array Vazio)")
    void buscarLogoEmpresa_NaoEncontrada() {
        ReflectionTestUtils.setField(service, "accessToken", "token");
        ReflectionTestUtils.setField(service, "tokenExpiration", LocalDateTime.now().plusHours(1));

        when(restTemplate.postForObject(any(URI.class), any(), eq(IgdbCompanyDto[].class)))
                .thenReturn(new IgdbCompanyDto[]{});

        String resultado = service.buscarLogoEmpresa("Empresa Fantasma");

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Deve retornar null e não quebrar se a API lançar exceção")
    void buscarLogoEmpresa_ErroApi() {
        ReflectionTestUtils.setField(service, "accessToken", "token");
        ReflectionTestUtils.setField(service, "tokenExpiration", LocalDateTime.now().plusHours(1));

        when(restTemplate.postForObject(any(URI.class), any(), any()))
                .thenThrow(new RestClientException("Erro de conexão"));

        String resultado = service.buscarLogoEmpresa("Erro");

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Deve renovar token se estiver expirado")
    void buscarLogoEmpresa_TokenExpirado() {
        ReflectionTestUtils.setField(service, "accessToken", "token-velho");
        ReflectionTestUtils.setField(service, "tokenExpiration", LocalDateTime.now().minusMinutes(10));

        TwitchAuthResponse authResponse = new TwitchAuthResponse("token-novo", 3600L);

        // CORREÇÃO NO WHEN
        when(restTemplate.postForObject(
                argThat((URI uri) -> uri != null && uri.toString().contains("twitch.tv")),
                isNull(),
                eq(TwitchAuthResponse.class))
        ).thenReturn(authResponse);

        when(restTemplate.postForObject(eq(URI.create(IGDB_URL + "/companies")), any(), eq(IgdbCompanyDto[].class)))
                .thenReturn(new IgdbCompanyDto[]{});

        service.buscarLogoEmpresa("Teste");

        // CORREÇÃO NO VERIFY
        verify(restTemplate, times(1)).postForObject(
                argThat((URI uri) -> uri != null && uri.toString().contains("twitch.tv")),
                any(),
                any());
    }
}