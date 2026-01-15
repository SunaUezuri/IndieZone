package br.com.lunix.services.usuario;

import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static final String ISSUER = "lunix-api";

    private final StringRedisTemplate redisTemplate;

    // Método para gerar o token inicial
    public String generateAccessToken(Usuario usuario) {
        return generateToken(usuario, 30);
    }

    // Método para gerar o token de refresh
    public String generateRefreshToken(Usuario usuario) {
        return generateToken(usuario, 60 * 24 * 7);
    }

    /*
        Método para gerar um token JWT para um usuário
        auntenticado

        @param usuario: Usuário a ser autenticado
        @return: Token JWT com as informações pertinentes ao usuário
    */
    public String generateToken(Usuario usuario, long minutesExpiration) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            List<String> roles = usuario.getRoles().stream()
                    .map(Role::name)
                    .collect(Collectors.toList());

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail())
                    .withClaim("role", roles)
                    .withClaim("id", usuario.getId())
                    .withExpiresAt(genExpirationDate(minutesExpiration))
                    .sign(algorithm);
        } catch (JWTCreationException e){
            throw new RuntimeException("Erro ao gerar o token JWT", e);
        }
    }

    /*
        Método para realizar a validação do token

        @param token - Token a ser validado
    */
    public String validateToken(String token) {
        try {
            if (isTokenBlackListed(token)) {
                return "";
            }

            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    /*
        Método para invalidar um token (LOGOUT)

        @param token - Token a ser invalidado
    */
    public void invalidateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm).withIssuer(ISSUER).build().verify(token);

            // Calcula quanto tempo falta para o token expirar
            Instant expiresAt = jwt.getExpiresAtAsInstant();
            long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();

            if (ttlSeconds > 0) {
                // Salva no Redis com TTL exato. Chave: "blacklist:{token}"
                redisTemplate.opsForValue().set("blacklist:" + token, "logout", Duration.ofSeconds(ttlSeconds));
            }
        } catch (JWTVerificationException e) {

        }
    }

    /*
        Método de geração de data de expiração

        @return: Retorna um LocalDateTime com o tempo para o token expirar
    */
    private Instant genExpirationDate(long minutes) {
        return Instant.now().plus(minutes, ChronoUnit.MINUTES);
    }

    /*
        Método para buscar o token no cache para ver se ele
        está válido ou se está na blackList

        @param token - Token a ser buscado
    */
    private boolean isTokenBlackListed(String token) {
        Boolean hasKey = redisTemplate.hasKey("blacklist:" + token);
        return Boolean.TRUE.equals(hasKey);
    }
}
