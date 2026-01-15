package br.com.lunix.services.dashboard;

import br.com.lunix.dto.dashboard.DashboardCompletoDto;
import br.com.lunix.dto.dashboard.DashboardConteudoDto;
import br.com.lunix.dto.dashboard.DashboardEngajamentoDto;
import br.com.lunix.dto.dashboard.DashboardUsuariosDto;
import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.mapper.JogoMapper;
import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.AvaliacaoRepository;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Service responsável por gerar dados de consulta em um Dashboard para ADMIN
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final JogoMapper jogoMapper;

    // Método para gerar todos o dados
    @Transactional(readOnly = true)
    public DashboardCompletoDto gerarDashboardCompleto() {
        return new DashboardCompletoDto(
                montarDadosUsuarios(),
                montarDadosConteudo(),
                montarDadosEngajamento()
        );
    }

    // Método para montar os dados de usuários
    private DashboardUsuariosDto montarDadosUsuarios() {
        long total = usuarioRepository.count();
        long novosMes = usuarioRepository.countByDataCriacaoAfter(LocalDateTime.now().minusDays(30));
        long desativados = usuarioRepository.countByAtivo(false);

        Map<String, Long> porRole = new HashMap<>();
        porRole.put("ADMIN", usuarioRepository.countByRolesContains(Role.ROLE_ADMIN));
        porRole.put("DEV", usuarioRepository.countByRolesContains(Role.ROLE_DEV));
        porRole.put("USER", usuarioRepository.countByRolesContains(Role.ROLE_USER));

        return new DashboardUsuariosDto(total, desativados, novosMes, porRole);
    }

    // Método para montar os dados de dashboard para o conteúdo principal
    private DashboardConteudoDto montarDadosConteudo() {
        long totalJogos = jogoRepository.count();

        long semPreco = jogoRepository.findByPrecosIsEmpty(Pageable.unpaged()).getTotalElements();

        long totalEmpresas = empresaRepository.count();

        // Cálculo aproximado de devs autônomos sem empresa vinculada
        long totalDevs = jogoRepository.countByEmpresaIsNull();

        // Cálculo de jogos separados por gênero
        Map<String, Long> porGenero = new HashMap<>();
        for (Genero g : Genero.values()) {
            long count = jogoRepository.countByGeneros(g);
            if (count > 0) porGenero.put(g.name(), count);
        }

        // Cálculo de jogos separados por plataforma
        Map<String, Long> porPlataforma = new HashMap<>();
        for (Plataforma p : Plataforma.values()) {
            long count = jogoRepository.countByPlataformas(p);
            if (count > 0) porPlataforma.put(p.name(), count);
        }

        List<JogoResponseDto> top5 = jogoRepository.findTop10ByOrderByNotaMediaDesc()
                .stream()
                .limit(5)
                .map(jogoMapper::toResponseDto)
                .toList();

        return new DashboardConteudoDto(
                totalJogos,
                semPreco,
                totalEmpresas,
                totalDevs,
                porGenero,
                porPlataforma,
                top5
        );
    }

    // Método para gerar os dados de engajamento ao Dashboard
    private DashboardEngajamentoDto montarDadosEngajamento() {
        long totalReviews = avaliacaoRepository.count();

        double mediaGlobal = avaliacaoRepository.calcularMediaGlobal();

        mediaGlobal = Math.round(mediaGlobal * 10.0) / 10.0;

        return new DashboardEngajamentoDto(totalReviews, mediaGlobal);
    }

}
