package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UsuarioRolePatchDto(
        @NotEmpty
        Set<Role> roles
) {
}
