package br.com.lunix.dto.usuario;

import br.com.lunix.dto.empresa.EmpresaResponseDto;

public record UsuarioProfileDto(
        String id,
        String nome,
        String email,
        EmpresaResponseDto empresa
) {
}
