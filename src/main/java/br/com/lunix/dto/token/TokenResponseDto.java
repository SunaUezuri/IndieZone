package br.com.lunix.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;

// DTO de resposta que devolve um token de acesso e um para dar refresh
public record TokenResponseDto(
        @Schema(
                description = "Token de autenticação JWT (Bearer)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBsdW5pe..."
        )
        String token,
        @Schema(
                description = "Token de autenticação JWT para refresh",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBsdW5pe..."
        )
        String refreshToken
) {
}
