package br.com.lunix.mapper;

import br.com.lunix.dto.usuario.*;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioMapperTest {

    private UsuarioMapper mapper;

    private Usuario usuarioBase;
    private Empresa empresaBase;

    @BeforeEach
    void setUp() {
        mapper = new UsuarioMapper();

        // Configura uma empresa padrão
        empresaBase = new Empresa();
        empresaBase.setId("emp-123");
        empresaBase.setNome("Lunix Corp");
        empresaBase.setPaisOrigem("Brasil");
        empresaBase.setUrlLogo("http://logo.com");

        // Configura um usuário padrão
        usuarioBase = new Usuario();
        usuarioBase.setId("user-001");
        usuarioBase.setNome("João Silva");
        usuarioBase.setEmail("joao@lunix.com");
        usuarioBase.setSenha("senha123");
        usuarioBase.setAtivo(true);
        usuarioBase.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));
    }

    @Test
    @DisplayName("Deve converter RegistroDto para Entidade com valores padrão")
    void toEntityDeveCriarUsuarioComPadroes() {
        // Cenário
        UsuarioRegistroDto dto = new UsuarioRegistroDto(
                "Maria",
                "maria@email.com",
                "12345678",
                "12345678",
                null
        );

        // Ação
        Usuario resultado = mapper.toEntity(dto);

        // Verificação
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(dto.nome());
        assertThat(resultado.getEmail()).isEqualTo(dto.email());
        assertThat(resultado.getSenha()).isEqualTo(dto.senha());

        // Verifica os valores padrão definidos no Mapper
        assertThat(resultado.isAtivo()).isTrue();
        assertThat(resultado.getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Deve retornar null se RegistroDto for nulo")
    void toEntityDeveRetornarNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Deve atualizar apenas os campos fornecidos no UpdateDto")
    void updateEntityFromDto_DeveAtualizarCampos() {
        // Cenário: Mudando nome, desativando e dando cargo de Admin
        Set<Role> novasRoles = Set.of(Role.ROLE_ADMIN, Role.ROLE_DEV);
        UsuarioUpdateDto dto = new UsuarioUpdateDto(
                "João Atualizado",
                novasRoles,
                false, // desativar
                null
        );

        // Ação
        mapper.updateEntityFromDto(dto, usuarioBase);

        // Verificação
        assertThat(usuarioBase.getNome()).isEqualTo("João Atualizado");
        assertThat(usuarioBase.isAtivo()).isFalse();
        assertThat(usuarioBase.getRoles()).containsExactlyInAnyOrder(Role.ROLE_ADMIN, Role.ROLE_DEV);
    }

    @Test
    @DisplayName("Não deve atualizar campos se o DTO tiver valores nulos")
    void updateEntityFromDtoNaoDeveAtualizarSeNulo() {
        // Cenário
        UsuarioUpdateDto dto = new UsuarioUpdateDto(
                "Novo Nome",
                null, // Roles null
                null, // Ativo null
                null
        );

        // Ação
        mapper.updateEntityFromDto(dto, usuarioBase);

        // Verificação
        assertThat(usuarioBase.getNome()).isEqualTo("Novo Nome"); // Mudou
        assertThat(usuarioBase.isAtivo()).isTrue(); // Mantém o original (true)
        assertThat(usuarioBase.getRoles()).containsExactly(Role.ROLE_USER); // Mantém o original
    }

    @Test
    @DisplayName("Deve converter para ProfileDto COM empresa vinculada")
    void toProfileDtoComEmpresa() {
        // Cenário: Vincula empresa ao usuário base
        usuarioBase.setEmpresa(empresaBase);

        // Ação
        UsuarioProfileDto dto = mapper.toProfileDto(usuarioBase);

        // Verificação
        assertThat(dto).isNotNull();
        assertThat(dto.nome()).isEqualTo(usuarioBase.getNome());
        assertThat(dto.email()).isEqualTo(usuarioBase.getEmail());

        // Verifica o objeto aninhado de empresa
        assertThat(dto.empresa()).isNotNull();
        assertThat(dto.empresa().id()).isEqualTo(empresaBase.getId());
        assertThat(dto.empresa().nome()).isEqualTo(empresaBase.getNome());
    }

    @Test
    @DisplayName("Deve converter para ProfileDto SEM empresa vinculada")
    void toProfileDtoSemEmpresa() {
        // Cenário: Usuário sem empresa (default do setUp)
        usuarioBase.setEmpresa(null);

        // Ação
        UsuarioProfileDto dto = mapper.toProfileDto(usuarioBase);

        // Verificação
        assertThat(dto.empresa()).isNull();
        assertThat(dto.email()).isEqualTo(usuarioBase.getEmail());
    }

    @Test
    @DisplayName("Deve converter para PublicProfileDto corretamente")
    void toPublicProfileDtoSucesso() {
        UsuarioPublicProfileDto dto = mapper.toPublicProfileDto(usuarioBase);

        assertThat(dto.id()).isEqualTo(usuarioBase.getId());
        assertThat(dto.nome()).isEqualTo(usuarioBase.getNome());
        // Garante que não vazou dados sensíveis
    }

    @Test
    @DisplayName("Deve converter para AdminListDto achatando dados da empresa")
    void toAdminListDtoComEmpresa() {
        // Cenário
        usuarioBase.setEmpresa(empresaBase);

        // Ação
        UsuarioAdminListDto dto = mapper.toAdminListDto(usuarioBase);

        // Verificação
        assertThat(dto.id()).isEqualTo(usuarioBase.getId());
        assertThat(dto.email()).isEqualTo(usuarioBase.getEmail());
        assertThat(dto.ativo()).isTrue();

        // Verifica o achatamento (Flattening)
        assertThat(dto.idEmpresa()).isEqualTo(empresaBase.getId());
        assertThat(dto.nomeEmpresa()).isEqualTo(empresaBase.getNome());
    }

    @Test
    @DisplayName("Deve converter para AdminListDto com campos de empresa nulos")
    void toAdminListDtoSemEmpresa() {
        usuarioBase.setEmpresa(null);

        UsuarioAdminListDto dto = mapper.toAdminListDto(usuarioBase);

        assertThat(dto.idEmpresa()).isNull();
        assertThat(dto.nomeEmpresa()).isNull();
        assertThat(dto.email()).isEqualTo(usuarioBase.getEmail());
    }
}