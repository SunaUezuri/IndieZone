package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/*
    DTO utilizado para exibir os usuários da aplicação
    para o ADMIN conseguir ter controle.
*/
public record UsuarioAdminListDto(
        @Schema(description = "ID do usuário", example = "69dg587ty1...")
        String id,
        @Schema(description = "Nome do usuário", example = "Leandro")
        String nome,
        @Schema(description = "E-mail cadastrado", example = "admin@lunix.com")
        String email,
        @Schema(description = "Roles de permissão do usuário", example = "ROLE_USER")
        Set<Role> roles,
        @Schema(description = "Status do usuário", example = "true")
        Boolean ativo,
        @Schema(description = "ID da empresa pertencente do usuário", example = "6985chji145...")
        String idEmpresa,
        @Schema(description = "Nome da empresa do usuário", example = "Team Cherry")
        String nomeEmpresa
) {
}
