package br.com.lunix.services.usuario;

import br.com.lunix.dto.usuario.*;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.UsuarioMapper;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService service;

    @Mock private UsuarioRepository repository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UsuarioMapper mapper;

    private Usuario usuario;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setNome("João");
        usuario.setEmail("joao@lunix.com");
        usuario.setAtivo(true);
        usuario.setRoles(Set.of(Role.ROLE_USER));

        empresa = new Empresa();
        empresa.setId("emp-1");
        empresa.setNome("Lunix Corp");
    }

    @Test
    @DisplayName("Deve listar todos os usuários paginados")
    void listarTodosSucesso() {
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        UsuarioAdminListDto listDto = mock(UsuarioAdminListDto.class);
        when(mapper.toAdminListDto(usuario)).thenReturn(listDto);

        Page<UsuarioAdminListDto> result = service.listarTodos(0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(mapper).toAdminListDto(usuario);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void buscarPorIdSucesso() {
        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));

        UsuarioProfileDto profileDto = new UsuarioProfileDto("user-1", "João", "joao@lunix.com", null);
        when(mapper.toProfileDto(usuario)).thenReturn(profileDto);

        UsuarioProfileDto result = service.buscarPorId("user-1");

        assertThat(result.nome()).isEqualTo("João");
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar ID inexistente")
    void buscarPorIdNaoEncontrado() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId("999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve atualizar usuário e vincular empresa (Admin)")
    void atualizarComEmpresa() {
        UsuarioUpdateDto dto = new UsuarioUpdateDto("João Novo", Set.of(Role.ROLE_DEV), true, "emp-1");

        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresa));
        when(repository.save(usuario)).thenReturn(usuario);

        service.atualizar("user-1", dto);

        verify(mapper).updateEntityFromDto(dto, usuario);
        assertThat(usuario.getEmpresa()).isEqualTo(empresa);
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("Deve remover empresa se o ID vier vazio (Admin)")
    void atualizarRemoverEmpresa() {
        usuario.setEmpresa(empresa); // Usuário já tinha empresa

        // DTO com idEmpresa vazio
        UsuarioUpdateDto dto = new UsuarioUpdateDto("João", Set.of(Role.ROLE_USER), true, "");

        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        service.atualizar("user-1", dto);

        assertThat(usuario.getEmpresa()).isNull();
    }

    @Test
    @DisplayName("Deve lançar erro se tentar vincular empresa inexistente")
    void atualizarEmpresaInexistente() {
        UsuarioUpdateDto dto = new UsuarioUpdateDto("João", Set.of(Role.ROLE_USER), true, "emp-999");

        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(empresaRepository.findById("emp-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar("user-1", dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa não encontrada");
    }

    @Test
    @DisplayName("Deve atualizar perfil próprio (Nome, Email e Senha)")
    void atualizarProprioPerfilCompleto() {
        UsuarioSelfUpdateDto dto = new UsuarioSelfUpdateDto("João Silva", "novo@email.com", "novaSenha123");

        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(repository.findByEmail("novo@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("novaSenha123")).thenReturn("encodedPass");
        when(repository.save(usuario)).thenReturn(usuario);

        service.atualizarProprioPerfil("user-1", dto);

        assertThat(usuario.getNome()).isEqualTo("João Silva");
        assertThat(usuario.getEmail()).isEqualTo("novo@email.com");
        assertThat(usuario.getSenha()).isEqualTo("encodedPass");
    }

    @Test
    @DisplayName("Deve impedir atualização se o novo e-mail já estiver em uso")
    void atualizarProprioPerfilEmailDuplicado() {
        UsuarioSelfUpdateDto dto = new UsuarioSelfUpdateDto("João", "ocupado@email.com", null);

        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(repository.findByEmail("ocupado@email.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> service.atualizarProprioPerfil("user-1", dto))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já está em uso");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve validar email se o usuário mantiver o mesmo email")
    void atualizarProprioPerfil_MesmoEmail() {
        UsuarioSelfUpdateDto dto = new UsuarioSelfUpdateDto("João", "joao@lunix.com", null);

        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        service.atualizarProprioPerfil("user-1", dto);

        verify(repository, never()).findByEmail(anyString());
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("Deve realizar exclusão lógica (desativar)")
    void desativar_Sucesso() {
        when(repository.findById("user-1")).thenReturn(Optional.of(usuario));

        service.desativar("user-1");

        assertThat(usuario.isAtivo()).isFalse();
        verify(repository).save(usuario);
    }
}