package br.com.lunix.services.dashboard;

import br.com.lunix.dto.dashboard.DashboardJogoDto;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.repository.JogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

// Service responsável por gerar dados de consulta em um Dashboard para ADMIN
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JogoRepository jogoRepository;

    @Transactional(readOnly = true)
    public DashboardJogoDto gerarDadosDashboard() {
        long totalJogos = jogoRepository.count();
        long semPreco = jogoRepository.findByPrecosIsEmpty(Pageable.unpaged()).getTotalElements();

        Map<String, Long> porGenero = new HashMap<>();
        for (Genero g : Genero.values()) {
            long count = jogoRepository.countByGeneros(g);
            if (count > 0) porGenero.put(g.name(), count);
        }

        Map<String, Long> porPlataforma = new HashMap<>();
        for (Plataforma p : Plataforma.values()) {
            long count = jogoRepository.countByPlataformas(p);
            if (count > 0) porPlataforma.put(p.name(), count);
        }

        return new DashboardJogoDto(totalJogos, semPreco, porGenero, porPlataforma);
    }
}
