package br.com.lunix.mapper;

import br.com.lunix.dto.empresa.EmpresaResponseDto;
import br.com.lunix.dto.usuario.*;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRegistroDto dto) {
        if (dto == null) return null;

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());

        // Padrões de inicialização
        usuario.setAtivo(true);
        usuario.getRoles().add(Role.ROLE_USER);
        return usuario;
    }

    public void updateEntityFromDto(UsuarioUpdateDto dto, Usuario usuario) {
        if (dto == null || usuario == null) return;

        usuario.setNome(dto.nome());

        if (dto.ativo() != null) {
            usuario.setAtivo(dto.ativo());
        }

        if (dto.roles() != null && !dto.roles().isEmpty()) {
            usuario.setRoles(dto.roles());
        }
    }

    public UsuarioProfileDto toProfileDto(Usuario usuario) {
        if (usuario == null) return null;

        EmpresaResponseDto empresaDto = null;

        if (usuario.getEmpresa() != null) {
            empresaDto = new EmpresaResponseDto(
                    usuario.getEmpresa().getId(),
                    usuario.getEmpresa().getNome(),
                    usuario.getEmpresa().getPaisOrigem(),
                    usuario.getEmpresa().getUrlLogo()
            );
        }
        return new UsuarioProfileDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                empresaDto
        );
    }

    public UsuarioPublicProfileDto toPublicProfileDto(Usuario usuario) {
        if (usuario == null) return null;

        return new UsuarioPublicProfileDto(
                usuario.getId(),
                usuario.getNome()
        );
    }

    public UsuarioAdminListDto toAdminListDto(Usuario usuario) {
        if (usuario == null) return null;

        String idEmpresa = null;
        String nomeEmpresa = null;

        if (usuario.getEmpresa() != null) {
            idEmpresa = usuario.getEmpresa().getId();
            nomeEmpresa = usuario.getEmpresa().getNome();
        }

        return new UsuarioAdminListDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRoles(),
                usuario.isAtivo(),
                idEmpresa,
                nomeEmpresa
        );
    }
}
