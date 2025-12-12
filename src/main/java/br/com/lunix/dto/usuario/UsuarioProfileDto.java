package br.com.lunix.dto.usuario;

import br.com.lunix.dto.empresa.EmpresaResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

/*
    DTO utilizado para a área de perfil do
    usuário.
*/
public record UsuarioProfileDto(
        @Schema(description = "ID do usuário", example = "69dg587ty1...")
        String id,
        @Schema(description = "Nome do usuário", example = "João")
        String nome,
        @Schema(description = "E-mail cadastrado", example = "admin@lunix.com")
        String email,
        EmpresaResponseDto empresa
) {
}
