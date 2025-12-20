package br.com.lunix.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

// DTO para devolver uma mensagem de erro mais agradável
public record StandardError(
        @Schema(description = "Momento que o erro ocorreu", example = "2025-12-12T20:49:23.637Z")
        Instant timestamp,
        @Schema(description = "Código de status", example = "404")
        Integer status,
        @Schema(description = "Tipo de erro encontrado", example = "Não encontrado")
        String error,
        @Schema(description = "Mensagem de erro", example = "Recurso não encontrado")
        String message,
        @Schema(description = "Caminho da origem do erro", example = "lunix/usuarios")
        String path
) {}
