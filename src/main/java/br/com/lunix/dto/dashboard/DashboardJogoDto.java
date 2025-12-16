package br.com.lunix.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

// Dto específico para o dashboard do admin
public record DashboardJogoDto(
        @Schema(description = "Total de jogos registrados na aplicação", example = "21")
        long totalJogos,
        // Campo útil para garantir que todos os jogos estão com o nome correto para a atualização automática
        @Schema(description = "Jogos que não tem preço cadastrado", example = "10")
        long totalJogosSemPreco,
        @Schema(description = "Quantidade de jogos que há de um certo gênero", example = "[ 'ACAO': 3 ]")
        Map<String, Long> jogosPorGenero,
        @Schema(description = "Quantidade de jogos que há de um certas plataformas", example = "[ 'PC': 5 ]")
        Map<String, Long> jogosPorPlataforma
) {}
