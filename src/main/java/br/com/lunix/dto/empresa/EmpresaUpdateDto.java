package br.com.lunix.dto.empresa;

import jakarta.validation.constraints.NotBlank;

public record EmpresaUpdateDto(
        @NotBlank
        String nome,
        @NotBlank
        String descricao,
        @NotBlank
        String paisOrigem,
        String urlLogo
) {
}
