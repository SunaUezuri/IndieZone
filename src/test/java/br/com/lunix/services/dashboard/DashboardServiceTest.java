package br.com.lunix.services.dashboard;

import br.com.lunix.dto.dashboard.DashboardCompletoDto;
import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.mapper.JogoMapper;
import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService service;

    @Mock private JogoRepository jogoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private JogoMapper jogoMapper;

    @Test
    @DisplayName("Deve gerar dashboard completo com dados preenchidos e cálculos corretos")
    void gerarDashboardDadosCompletos() {
        when(usuarioRepository.count()).thenReturn(100L);
        when(usuarioRepository.countByAtivo(false)).thenReturn(5L);
        when(usuarioRepository.countByDataCriacaoAfter(any(LocalDateTime.class))).thenReturn(10L);
        when(usuarioRepository.countByRolesContains(Role.ROLE_ADMIN)).thenReturn(2L);
        when(usuarioRepository.countByRolesContains(Role.ROLE_DEV)).thenReturn(20L);
        when(usuarioRepository.countByRolesContains(Role.ROLE_USER)).thenReturn(78L);

        when(jogoRepository.count()).thenReturn(50L);
        when(empresaRepository.count()).thenReturn(15L);
        when(jogoRepository.countByEmpresaIsNull()).thenReturn(5L); // Devs autônomos

        // Jogos sem preço (retorna Page)
        when(jogoRepository.findByPrecosIsEmpty(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Jogo(), new Jogo()))); // 2 jogos sem preço

        // Top 5 jogos (Mock do stream/map)
        Jogo jogoMock = new Jogo();
        when(jogoRepository.findTop10ByOrderByNotaMediaDesc()).thenReturn(List.of(jogoMock));
        when(jogoMapper.toResponseDto(any(Jogo.class))).thenReturn(mock(JogoResponseDto.class));

        // Mocks de Gênero e Plataforma
        when(jogoRepository.countByGeneros(any())).thenReturn(0L);
        when(jogoRepository.countByGeneros(Genero.RPG)).thenReturn(10L);

        when(jogoRepository.countByPlataformas(any())).thenReturn(0L);
        when(jogoRepository.countByPlataformas(Plataforma.PC)).thenReturn(25L);

        when(avaliacaoRepository.count()).thenReturn(2L);

        when(avaliacaoRepository.calcularMediaGlobal()).thenReturn(9.0);

        DashboardCompletoDto result = service.gerarDashboardCompleto();

        assertThat(result).isNotNull();

        // Valida Usuários
        assertThat(result.usuarios().totalUsuarios()).isEqualTo(100);
        assertThat(result.usuarios().distribuicaoPorRole()).containsEntry("ADMIN", 2L);

        // Valida Conteúdo
        assertThat(result.conteudo().totalJogos()).isEqualTo(50);
        assertThat(result.conteudo().totalJogosSemPreco()).isEqualTo(2);
        assertThat(result.conteudo().jogosPorGenero()).containsEntry("RPG", 10L);
        assertThat(result.conteudo().jogosPorGenero()).doesNotContainKey("ACAO");
        assertThat(result.conteudo().jogosPorPlataforma()).containsEntry("PC", 25L);

        // Valida Engajamento
        assertThat(result.engajamento().totalAvaliacoes()).isEqualTo(2);
        assertThat(result.engajamento().mediaGlobalNotas()).isEqualTo(9.0);
    }

    @Test
    @DisplayName("Deve gerar dashboard com zeros quando não houver dados (Edge Case)")
    void gerarDashboardSemDados() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.countByAtivo(false)).thenReturn(0L);
        when(usuarioRepository.countByDataCriacaoAfter(any())).thenReturn(0L);
        when(usuarioRepository.countByRolesContains(any())).thenReturn(0L);

        when(jogoRepository.count()).thenReturn(0L);
        when(empresaRepository.count()).thenReturn(0L);
        when(jogoRepository.countByEmpresaIsNull()).thenReturn(0L);

        when(jogoRepository.findByPrecosIsEmpty(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList())); // 0 jogos sem preço

        when(jogoRepository.findTop10ByOrderByNotaMediaDesc()).thenReturn(Collections.emptyList());

        when(jogoRepository.countByGeneros(any())).thenReturn(0L);
        when(jogoRepository.countByPlataformas(any())).thenReturn(0L);

        when(avaliacaoRepository.count()).thenReturn(0L);
        when(avaliacaoRepository.calcularMediaGlobal()).thenReturn(0.0); // Lista vazia para testar média 0

        DashboardCompletoDto result = service.gerarDashboardCompleto();

        assertThat(result).isNotNull();

        // Usuários zerados
        assertThat(result.usuarios().totalUsuarios()).isZero();
        assertThat(result.usuarios().distribuicaoPorRole().get("ADMIN")).isZero();

        // Conteúdo zerado e mapas vazios
        assertThat(result.conteudo().totalJogos()).isZero();
        assertThat(result.conteudo().jogosPorGenero()).isEmpty();
        assertThat(result.conteudo().jogosPorPlataforma()).isEmpty();

        // Média deve ser 0.0 (evitou divisão por zero)
        assertThat(result.engajamento().mediaGlobalNotas()).isEqualTo(0.0);
    }
}