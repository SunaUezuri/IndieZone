package br.com.lunix.dto.jogos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

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
