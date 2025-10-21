package br.com.lunix.dto.empresa;

import jakarta.validation.constraints.NotBlank;

public record EmpresaRequestDto(
        @NotBlank(message = "O nome da empresa não pode ser vazio")
        String nome,
        @NotBlank(message = "A empresa deve ter uma descrição")
        String descricao,
        @NotBlank(message = "A empresa deve ter um país de origem definido")
        String paisOrigem,
        String urlLogo
) {
}
