package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/*
    DTO para se utilizar no método de PATCH
    para a alteração dos cargos de um usuário.

    @NotEmpty - Indica que o campo não pode estar
    vazio.
*/
public record UsuarioRolePatchDto(
        @Schema(description = "Role do usuário", example = "[ ROLE_USER ]")
        @NotEmpty(message = "O usuário deve ter uma Role")
        Set<Role> roles
) {
}
