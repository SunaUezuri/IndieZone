package br.com.lunix.dto.dashboard;

import java.util.Map;

// Dto específico para o dashboard do admin
public record DashboardJogoDto(
        long totalJogos,
        long totalJogosSemPreco,
        Map<String, Long> jogosPorGenero
) {}
