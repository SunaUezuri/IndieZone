package br.com.lunix.dto.empresa;

import jakarta.validation.constraints.NotBlank;

/*
    DTO utilizado para o formulário de atualização
    de dados de uma empresa.

    @NotBlank - Garante que o campo não seja vazio.
*/
public record EmpresaUpdateDto(
        @NotBlank(message = "O nome da empresa não pode ser vazio")
        String nome,
        @NotBlank(message = "A empresa deve ter uma descrição")
        String descricao,
        @NotBlank(message = "A empresa deve ter um país de origem definido")
        String paisOrigem,
        String urlLogo
) {
}
