package br.com.lunix.dto.usuario;

import br.com.lunix.dto.empresa.EmpresaResponseDto;

/*
    DTO utilizado para a área de perfil do
    usuário.
*/
public record UsuarioProfileDto(
        String id,
        String nome,
        String email,
        EmpresaResponseDto empresa
) {
}
