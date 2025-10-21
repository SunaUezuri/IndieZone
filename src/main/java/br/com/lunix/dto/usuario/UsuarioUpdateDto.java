package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UsuarioUpdateDto(
        @NotBlank
        String nome,
        @NotEmpty
        Set<Role> roles,
        @NotNull
        Boolean ativo,
        String idEmpresa
) {
}
