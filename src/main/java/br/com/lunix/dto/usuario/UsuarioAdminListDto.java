package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;

import java.util.Set;

/*
    DTO utilizado para exibir os usuários da aplicação
    para o ADMIN conseguir ter controle.
*/
public record UsuarioAdminListDto(
        String id,
        String email,
        Set<Role> roles,
        Boolean ativo,
        String idEmpresa,
        String nomeEmpresa
) {
}
