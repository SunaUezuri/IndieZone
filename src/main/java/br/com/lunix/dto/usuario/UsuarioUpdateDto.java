package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UsuarioUpdateDto(
        @NotBlank(message = "O nome não pode ser vazio")
        String nome,
        @NotEmpty(message = "O usuário deve ter uma Role")
        Set<Role> roles,
        @NotNull(message = "É necessário dizer se o usuário é ativo ou não")
        Boolean ativo,
        String idEmpresa
) {
}
