package br.com.lunix.dto.jogos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// DTO para o request de cadastro de jogos para admin e dev
public record JogoAdminRequestDto(
        @NotNull
        @Schema(description = "Informações do jogo a serem inseridas")
        JogoRequestDto jogoData,
        @Schema(description = "Identificador único de um desenvolvedor", example = "650c...")
        String devId,
        @Schema(description = "Identificador único de uma empresa", example = "789c...")
        String empresaIdExistente
) {
}
