package br.com.lunix.dto.empresa;

import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.model.entities.Empresa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/*
    DTO que trás detalhes sobre a empresa
    exibida
*/
public record EmpresaDetalhesDto(
        @Schema(description = "Identificador único da empresa", example = "650c...")
        String id,
        @Schema(description = "Nome da empresa desenvolvedora", example = "Team Cherry")
        String nome,
        @Schema(description = "Descrição da empresa cadastrada", example = "Empresa de pequeno porte da Austrália")
        String descricao,
        @Schema(description = "País onde foi criada a empresa/estúdio", example = "Austrália")
        String paisOrigem,
        @Schema(description = "URL para uma imagem da logo da empresa", example = "https://img.com")
        String urlLogo,
        @Schema(description = "Lista com os jogos pertencentes a empresa")
        List<JogoResponseDto> jogos
) {}
