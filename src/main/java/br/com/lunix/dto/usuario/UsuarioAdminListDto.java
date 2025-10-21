package br.com.lunix.dto.usuario;

import br.com.lunix.model.enums.Role;

import java.util.Set;

public record UsuarioAdminListDto(
        String id,
        String email,
        Set<Role> roles,
        Boolean ativo,
        String idEmpresa,
        String nomeEmpresa
) {
}
