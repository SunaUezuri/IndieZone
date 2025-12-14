package br.com.lunix.dto.jogos;

import jakarta.validation.constraints.NotNull;

public record JogoAdminRequestDto(
        @NotNull
        JogoRequestDto jogoData,
        String empresaIdExistente
) {
}
