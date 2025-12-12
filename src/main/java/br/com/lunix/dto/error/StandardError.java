package br.com.lunix.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record StandardError(
        @Schema(description = "Momento que o erro ocorreu", example = "2025-12-12T20:49:23.637Z")
        Instant timestamp,
        @Schema(description = "Código de status", example = "200")
        Integer status,
        @Schema(description = "Tipo de erro encontrado", example = "Regra de negócio")
        String error,
        @Schema(description = "Mensagem de erro", example = "E-mail duplicado")
        String message,
        @Schema(description = "Caminho da origem do erro", example = "lunix/usuarios")
        String path
) {}
