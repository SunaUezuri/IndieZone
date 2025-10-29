package br.com.lunix.dto.empresa;

import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.model.entities.Empresa;

import java.util.List;

/*
    DTO que trás detalhes sobre a empresa
    exibida
*/
public record EmpresaDetalhesDto(Empresa empresa, List<JogoResponseDto> jogos) {
}
