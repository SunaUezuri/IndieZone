package br.com.lunix.dto.empresa;

import br.com.lunix.model.entities.Empresa;

public record EmpresaDetalhesDto(Empresa empresa, List<JogoResponseDTO> jogos) {
}
