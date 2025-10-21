package br.com.lunix.dto.empresa;

import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.model.entities.Empresa;

import java.util.List;

public record EmpresaDetalhesDto(Empresa empresa, List<JogoResponseDto> jogos) {
}
