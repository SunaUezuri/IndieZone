package br.com.lunix.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// Dto para o reset do token JWT
public record RefreshTokenRequestDto(
        @NotBlank(message = "O Refresh Token é obrigatório")
        @Schema(
                description = "Token de autenticação JWT para refresh",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBsdW5pe..."
        )
        String refreshToken
) {
}
