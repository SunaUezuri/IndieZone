package br.com.lunix.services.avaliacao;

import br.com.lunix.dto.avaliacao.AvaliacaoRequestDto;
import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.avaliacao.AvaliacaoUpdateDto;
import br.com.lunix.dto.avaliacao.ResultadoAgregacaoDto;
import br.com.lunix.exceptions.AutoAvaliacaoException;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.AvaliacaoMapper;
import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.AvaliacaoRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.services.jogo.JogoSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
class AvaliacaoServiceTest {

    @InjectMocks
    private AvaliacaoService service;

    @Mock
    private AvaliacaoRepository repository;
    @Mock
    private JogoRepository jogoRepository;
    @Mock
    private AvaliacaoMapper mapper;
    @Mock
    private JogoSecurityService securityService;

    // Objetos base
    private Usuario usuarioComum;
    private Usuario usuarioDev;
    private Usuario usuarioAdmin;
    private Jogo jogo;
    private Avaliacao avaliacao;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        // Configura Usuário Comum
        usuarioComum = new Usuario();
        usuarioComum.setId("user-1");
        usuarioComum.setNome("User Test");
        usuarioComum.setRoles(Set.of(Role.ROLE_USER));

        // Configura Usuário Dev
        usuarioDev = new Usuario();
        usuarioDev.setId("dev-1");
        usuarioDev.setNome("Dev Test");
        usuarioDev.setRoles(Set.of(Role.ROLE_DEV));

        // Configura Admin
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId("admin-1");
        usuarioAdmin.setNome("Admin");
        usuarioAdmin.setRoles(Set.of(Role.ROLE_ADMIN));

        // Configura Empresa
        empresa = new Empresa();
        empresa.setId("emp-1");
        empresa.setNome("Lunix Arts");

        // Configura Jogo
        jogo = new Jogo();
        jogo.setId("game-1");
        jogo.setTitulo("Super Game");
        jogo.setNotaMedia(0.0);
        jogo.setTotalAvaliacoes(0);

        // Configura Avaliação
        avaliacao = new Avaliacao();
        avaliacao.setId("av-1");
        avaliacao.setNota(10.0);
        avaliacao.setComentario("Top");
        avaliacao.setUsuario(usuarioComum);
        avaliacao.setJogo(jogo);
    }

    @Test
    @DisplayName("Deve criar avaliação com sucesso e atualizar estatísticas")
    void criarSucesso() {
        // Arrange
        AvaliacaoRequestDto requestDto = new AvaliacaoRequestDto(9.5, "Bom jogo");
        ResultadoAgregacaoDto agregacaoDto = new ResultadoAgregacaoDto(jogo.getId(), 9.5, 1);
        AvaliacaoResponseDto responseDto = new AvaliacaoResponseDto("av-1", 9.5, "Bom jogo", null, null);

        when(securityService.getUsuarioLogado()).thenReturn(usuarioComum);
        when(jogoRepository.findById("game-1")).thenReturn(Optional.of(jogo));
        when(repository.existsByUsuarioAndJogo(usuarioComum, jogo)).thenReturn(false); // Não avaliou ainda
        when(mapper.toEntity(requestDto)).thenReturn(avaliacao);
        when(repository.save(any(Avaliacao.class))).thenReturn(avaliacao);

        // Mock do recálculo de média
        when(repository.calcularMediaDoJogo("game-1")).thenReturn(agregacaoDto);
        when(mapper.toResponseDto(avaliacao)).thenReturn(responseDto);

        // Act
        AvaliacaoResponseDto result = service.criar("game-1", requestDto);

        // Assert
        assertThat(result).isNotNull();
        verify(repository).save(any(Avaliacao.class));

        // Verifica se atualizou o jogo com os dados da agregação
        verify(jogoRepository).save(jogo);
        assertThat(jogo.getNotaMedia()).isEqualTo(9.5);
        assertThat(jogo.getTotalAvaliacoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve lançar exceção se o jogo não existir")
    void criarJogoNaoEncontrado() {
        when(securityService.getUsuarioLogado()).thenReturn(usuarioComum);
        when(jogoRepository.findById("game-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar("game-999", new AvaliacaoRequestDto(10.0, "a")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Jogo não encontrado");
    }

    @Test
    @DisplayName("Deve impedir avaliação duplicada (Regra de Negócio)")
    void criarAvaliacaoDuplicada() {
        when(securityService.getUsuarioLogado()).thenReturn(usuarioComum);
        when(jogoRepository.findById("game-1")).thenReturn(Optional.of(jogo));
        when(repository.existsByUsuarioAndJogo(usuarioComum, jogo)).thenReturn(true); // Já avaliou

        assertThatThrownBy(() -> service.criar("game-1", new AvaliacaoRequestDto(10.0, "a")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Você já avaliou este jogo");
    }

    @Test
    @DisplayName("Deve impedir Dev Autônomo de avaliar o próprio jogo (AutoAvaliacao)")
    void criarConflitoInteresseDevAutonomo() {
        jogo.setDevAutonomo(usuarioDev);

        when(securityService.getUsuarioLogado()).thenReturn(usuarioDev);
        when(jogoRepository.findById("game-1")).thenReturn(Optional.of(jogo));
        when(repository.existsByUsuarioAndJogo(usuarioDev, jogo)).thenReturn(false);

        assertThatThrownBy(() -> service.criar("game-1", new AvaliacaoRequestDto(10.0, "a")))
                .isInstanceOf(AutoAvaliacaoException.class)
                .hasMessageContaining("Você é o criador deste jogo");
    }

    @Test
    @DisplayName("Deve impedir membro da Empresa de avaliar jogo da empresa (AutoAvaliacao)")
    void criarConflitoInteresseEmpresa() {
        // Jogo da empresa X, usuário também da empresa X
        jogo.setEmpresa(empresa);
        usuarioComum.setEmpresa(empresa);

        when(securityService.getUsuarioLogado()).thenReturn(usuarioComum);
        when(jogoRepository.findById("game-1")).thenReturn(Optional.of(jogo));

        assertThatThrownBy(() -> service.criar("game-1", new AvaliacaoRequestDto(10.0, "a")))
                .isInstanceOf(AutoAvaliacaoException.class)
                .hasMessageContaining("faz parte da empresa");
    }

    @Test
    @DisplayName("Deve atualizar avaliação com sucesso se for o autor")
    void atualizarSucesso() {
        AvaliacaoUpdateDto updateDto = new AvaliacaoUpdateDto(5, "Mudando nota");

        when(repository.findById("av-1")).thenReturn(Optional.of(avaliacao));
        when(securityService.getUsuarioLogado()).thenReturn(usuarioComum); // Autor da review
        when(repository.save(avaliacao)).thenReturn(avaliacao);

        // Mock do recálculo após update
        when(repository.calcularMediaDoJogo(jogo.getId())).thenReturn(new ResultadoAgregacaoDto(jogo.getId(), 5.0, 1));

        service.atualizar("av-1", updateDto);

        verify(mapper).updateFromEntityDto(updateDto, avaliacao);
        verify(repository).save(avaliacao);
        verify(jogoRepository).save(jogo);
    }

    @Test
    @DisplayName("Deve impedir atualização se usuário não for o autor")
    void atualizarSemPermissao() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId("user-2");

        when(repository.findById("av-1")).thenReturn(Optional.of(avaliacao));
        when(securityService.getUsuarioLogado()).thenReturn(outroUsuario);

        assertThatThrownBy(() -> service.atualizar("av-1", new AvaliacaoUpdateDto(5, "a")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não tem permissão");
    }

    @Test
    @DisplayName("Autor deve conseguir deletar sua avaliação")
    void deletarAutorSucesso() {
        when(repository.findById("av-1")).thenReturn(Optional.of(avaliacao));
        when(securityService.getUsuarioLogado()).thenReturn(usuarioComum); // Autor

        service.deletar("av-1");

        verify(repository).delete(avaliacao);
        verify(repository).calcularMediaDoJogo(jogo.getId()); // Recalculou
    }

    @Test
    @DisplayName("Admin deve conseguir deletar qualquer avaliação")
    void deletarAdminSucesso() {
        when(repository.findById("av-1")).thenReturn(Optional.of(avaliacao));
        when(securityService.getUsuarioLogado()).thenReturn(usuarioAdmin); // É Admin, não é autor

        service.deletar("av-1");

        verify(repository).delete(avaliacao);
    }

    @Test
    @DisplayName("Terceiro não deve conseguir deletar avaliação alheia")
    void deletarSemPermissao() {
        Usuario hacker = new Usuario();
        hacker.setId("hacker");
        hacker.setRoles(Set.of(Role.ROLE_USER));

        when(repository.findById("av-1")).thenReturn(Optional.of(avaliacao));
        when(securityService.getUsuarioLogado()).thenReturn(hacker);

        assertThatThrownBy(() -> service.deletar("av-1"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não tem permissão");
    }

    @Test
    @DisplayName("Deve listar avaliações por jogo paginadas")
    void listarPorJogoSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Avaliacao> pageAvaliacao = new PageImpl<>(List.of(avaliacao));

        when(jogoRepository.findById("game-1")).thenReturn(Optional.of(jogo));
        when(repository.findByJogo(eq(jogo), any(Pageable.class))).thenReturn(pageAvaliacao);

        Page<AvaliacaoResponseDto> result = service.listarPorJogo("game-1", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(mapper).toResponseDto(avaliacao);
    }

    @Test
    @DisplayName("Deve listar minhas avaliações")
    void listarMinhasSucesso() {
        Page<Avaliacao> pageAvaliacao = new PageImpl<>(List.of(avaliacao));

        when(securityService.getUsuarioLogado()).thenReturn(usuarioComum);
        when(repository.findByUsuario(eq(usuarioComum), any(Pageable.class))).thenReturn(pageAvaliacao);

        Page<AvaliacaoResponseDto> result = service.listarMinhas(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }
}