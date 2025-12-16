package br.com.lunix.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;

// DTO que mostra a empresa dados de uma empresa
public record EmpresaResponseDto(
        @Schema(description = "Identificador único da empresa", example = "650c...")
        String id,
        @Schema(description = "Nome da empresa desenvolvedora", example = "Team Cherry")
        String nome,
        @Schema(description = "País onde foi criada a empresa/estúdio", example = "Austrália")
        String paisOrigem,
        @Schema(description = "URL para uma imagem da logo da empresa", example = "https://img.com")
        String urlLogo) {
}
