package br.com.lunix.services.jogo;

import br.com.lunix.dto.jogos.*;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.JogoMapper;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.JogoRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JogoServiceTest {

    @InjectMocks
    private JogoService service;

    @Mock private JogoRepository jogoRepository;
    @Mock private JogoMapper jogoMapper;
    @Mock private JogoSecurityService securityService;
    @Mock private JogoPrecoService precoService;

    private Jogo jogo;
    private JogoRequestDto requestDto;
    private JogoResponseDto responseDto;
    private Usuario usuarioDev;

    @BeforeEach
    void setUp() {
        jogo = new Jogo();
        jogo.setId("game-1");
        jogo.setTitulo("Celeste");

        usuarioDev = new Usuario();
        usuarioDev.setId("dev-1");
        usuarioDev.setRoles(Set.of(Role.ROLE_DEV));

        requestDto = mock(JogoRequestDto.class);
        responseDto = new JogoResponseDto("game-1", "Celeste", "url", "Dev", 10.0, null, null);
    }

    @Test
    @DisplayName("Deve cadastrar jogo com sucesso e disparar fila de preços")
    void cadastrarSucesso() {
        // Arrange
        JogoAdminRequestDto adminRequest = new JogoAdminRequestDto(requestDto, null, null);

        when(jogoMapper.toEntity(requestDto)).thenReturn(jogo);
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogoMapper.toResponseDto(jogo)).thenReturn(responseDto);

        // Act
        JogoResponseDto result = service.cadastrar(adminRequest);

        // Assert
        assertThat(result).isNotNull();
        // Verifica se delegou a definição de dono
        verify(securityService).definirDonoDoJogo(jogo, adminRequest);
        // Verifica se salvou
        verify(jogoRepository).save(jogo);
        // Verifica se enviou para a fila de preços
        verify(precoService).enviarParaFila("game-1");
    }

    @Test
    @DisplayName("Deve atualizar jogo com sucesso se tiver permissão")
    void atualizarSucesso() {
        JogoUpdateDto updateDto = mock(JogoUpdateDto.class);

        when(jogoRepository.findById("game-1")).thenReturn(Optional.of(jogo));
        when(jogoRepository.save(jogo)).thenReturn(jogo);
        when(jogoMapper.toResponseDto(jogo)).thenReturn(responseDto);

        // Act
        service.atualizar("game-1", updateDto);

        // Assert
        verify(securityService).validarPermissaoEdicao(jogo);
        verify(jogoMapper).updateEntityFromDto(updateDto, jogo);
        verify(jogoRepository).save(jogo);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar atualizar jogo inexistente")
    void atualizarNaoEncontrado() {
        when(jogoRepository.findById("invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar("invalido", mock(JogoUpdateDto.class)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(securityService); // Nem deve validar permissão se não achou
    }

    @Test
    @DisplayName("Deve deletar jogo com sucesso")
    void deletarSucesso() {
        when(jogoRepository.findById("game-1")).thenReturn(Optional.of(jogo));

        service.deletar("game-1");

        verify(securityService).validarPermissaoEdicao(jogo);
        verify(jogoRepository).delete(jogo);
    }

    @Test
    @DisplayName("Deve buscar jogos por gênero paginado")
    void buscarPorGeneroSucesso() {
        Page<Jogo> page = new PageImpl<>(List.of(jogo));
        when(jogoRepository.findByGeneros(eq(Genero.RPG), any(Pageable.class))).thenReturn(page);

        Page<JogoResponseDto> result = service.buscarPorGenero(Genero.RPG, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar jogos por plataforma paginado")
    void buscarPorPlataformaSucesso() {
        Page<Jogo> page = new PageImpl<>(List.of(jogo));
        when(jogoRepository.findByPlataformas(eq(Plataforma.PC), any(Pageable.class))).thenReturn(page);

        Page<JogoResponseDto> result = service.buscarPorPlataforma(Plataforma.PC, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("listarMeusJogos: Admin deve ver TODOS os jogos")
    void listarMeusJogosAdmin() {
        Usuario admin = new Usuario();
        admin.setRoles(Set.of(Role.ROLE_ADMIN));

        when(securityService.getUsuarioLogado()).thenReturn(admin);
        when(jogoRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(jogo)));

        service.listarMeusJogos(0, 10);

        verify(jogoRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("listarMeusJogos: Dev Autônomo deve ver apenas seus jogos")
    void listarMeusJogosDevAutonomo() {
        when(securityService.getUsuarioLogado()).thenReturn(usuarioDev);
        when(jogoRepository.findByDevAutonomo(eq(usuarioDev), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(jogo)));

        service.listarMeusJogos(0, 10);

        verify(jogoRepository).findByDevAutonomo(eq(usuarioDev), any(Pageable.class));
    }

    @Test
    @DisplayName("listarMeusJogos: Dev de Empresa deve ver jogos da empresa")
    void listarMeusJogosEmpresa() {
        Usuario devEmpresa = new Usuario();
        devEmpresa.setRoles(Set.of(Role.ROLE_DEV));
        Empresa empresa = new Empresa();
        devEmpresa.setEmpresa(empresa);

        when(securityService.getUsuarioLogado()).thenReturn(devEmpresa);
        when(jogoRepository.findByEmpresa(eq(empresa), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(jogo)));

        service.listarMeusJogos(0, 10);

        verify(jogoRepository).findByEmpresa(eq(empresa), any(Pageable.class));
    }
}