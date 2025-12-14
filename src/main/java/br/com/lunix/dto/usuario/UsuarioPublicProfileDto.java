package br.com.lunix.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

// DTO para o perfil público de um usuário.
public record UsuarioPublicProfileDto(
        @Schema(description = "ID do usuário", example = "69dg587ty1...")
        String id,
        @Schema(description = "Nome do usuário", example = "João")
        String nome
) implements Serializable {
}
