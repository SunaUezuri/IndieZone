package br.com.lunix.services.usuario;

import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private Usuario usuario;
    private final String SECRET = "minha-chave-secreta-teste";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setEmail("test@lunix.com");
        usuario.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));
    }

    @Test
    @DisplayName("Deve gerar Access Token com validade de ~30 minutos")
    void generateAccessTokenSucesso() {
        String token = tokenService.generateAccessToken(usuario);

        assertThat(token).isNotNull().isNotEmpty();

        var decoded = JWT.require(Algorithm.HMAC256(SECRET))
                .withIssuer("lunix-api")
                .build()
                .verify(token);

        assertThat(decoded.getSubject()).isEqualTo("test@lunix.com");

        Instant expiracaoEsperada = Instant.now().plus(30, ChronoUnit.MINUTES);
        assertThat(decoded.getExpiresAt().toInstant())
                .isCloseTo(expiracaoEsperada, within(5, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve gerar Refresh Token com validade de ~7 dias")
    void generateRefreshTokenSucesso() {
        String token = tokenService.generateRefreshToken(usuario);

        assertThat(token).isNotNull().isNotEmpty();

        var decoded = JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token);

        Instant expiracaoEsperada = Instant.now().plus(7, ChronoUnit.DAYS);
        assertThat(decoded.getExpiresAt().toInstant())
                .isCloseTo(expiracaoEsperada, within(5, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("validateToken: Deve retornar o subject (email) se o token for válido e não estiver na blacklist")
    void validateTokenSucesso() {
        // Gera um token real válido
        String tokenValido = tokenService.generateAccessToken(usuario);

        // Mocka o Redis para dizer que o token NÃO está na blacklist
        when(redisTemplate.hasKey("blacklist:" + tokenValido)).thenReturn(false);

        // Valida
        String subject = tokenService.validateToken(tokenValido);

        assertThat(subject).isEqualTo("test@lunix.com");
    }

    @Test
    @DisplayName("validateToken: Deve retornar vazio se o token estiver na Blacklist")
    void validateTokenBlacklisted() {
        String token = tokenService.generateAccessToken(usuario);

        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(true);

        String result = tokenService.validateToken(token);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("validateToken: Deve retornar vazio se o token for inválido (assinatura errada)")
    void validateTokenAssinaturaInvalida() {
        String tokenFalso = JWT.create()
                .withIssuer("lunix-api")
                .sign(Algorithm.HMAC256("segredo-errado"));

        String result = tokenService.validateToken(tokenFalso);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("validateToken: Deve retornar vazio se o token estiver expirado")
    void validateTokenExpirado() {
        String tokenExpirado = JWT.create()
                .withIssuer("lunix-api")
                .withExpiresAt(Instant.now().minusSeconds(3600))
                .sign(Algorithm.HMAC256(SECRET));

        String result = tokenService.validateToken(tokenExpirado);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("invalidateToken: Deve calcular TTL e salvar na Blacklist do Redis")
    void invalidateTokenSucesso() {
        String token = tokenService.generateAccessToken(usuario);

        tokenService.invalidateToken(token);

        verify(valueOperations).set(
                eq("blacklist:" + token),
                eq("logout"),
                any(Duration.class)
        );
    }

    @Test
    @DisplayName("invalidateToken: Não deve fazer nada se o token for inválido (exceção engolida)")
    void invalidateTokenTokenInvalido() {
        String tokenInvalido = "token.mal.formado";

        tokenService.invalidateToken(tokenInvalido);

        verifyNoInteractions(valueOperations);
    }
}