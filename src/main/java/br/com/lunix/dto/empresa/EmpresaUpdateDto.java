package br.com.lunix.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/*
    DTO utilizado para o formulário de atualização
    de dados de uma empresa.

    @NotBlank - Garante que o campo não seja vazio.
*/
public record EmpresaUpdateDto(
        @Schema(description = "Nome da empresa desenvolvedora", example = "Team Cherry")
        @NotBlank(message = "O nome da empresa não pode ser vazio")
        String nome,
        @NotBlank(message = "A empresa deve ter uma descrição")
        @Schema(description = "Descrição da empresa cadastrada", example = "Empresa de pequeno porte da Austrália")
        String descricao,
        @NotBlank(message = "A empresa deve ter um país de origem definido")
        @Schema(description = "País onde foi criada a empresa/estúdio", example = "Austrália")
        String paisOrigem,
        @Schema(description = "URL para uma imagem da logo da empresa", example = "https://img.com")
        String urlLogo
) {
}
