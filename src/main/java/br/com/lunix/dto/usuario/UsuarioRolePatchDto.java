package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/*
    DTO para se utilizar no método de PATCH
    para a alteração dos cargos de um usuário.

    @NotEmpty - Indica que o campo não pode estar
    vazio.
*/
public record UsuarioRolePatchDto(
        @NotEmpty(message = "O usuário deve ter uma Role")
        Set<Role> roles
) {
}
