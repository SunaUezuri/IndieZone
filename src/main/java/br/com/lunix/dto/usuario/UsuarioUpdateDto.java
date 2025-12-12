package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/*
    DTO utilizado para atualização de usuários.

    @NotBlack e @NotNull - Anotações que garantem que o campo
    não esteja vazio.
*/
public record UsuarioUpdateDto(
        @Schema(description = "Nome do usuário", example = "João")
        @NotBlank(message = "O nome não pode ser vazio")
        String nome,
        @Schema(description = "Role do usuário", example = "[ ROLE_USER ]")
        @NotEmpty(message = "O usuário deve ter uma Role")
        Set<Role> roles,
        @Schema(description = "Status do usuário", example = "true")
        @NotNull(message = "É necessário dizer se o usuário é ativo ou não")
        Boolean ativo,
        @Schema(description = "ID da empresa pertencente do usuário", example = "6985chji145...")
        String idEmpresa
) {
}
