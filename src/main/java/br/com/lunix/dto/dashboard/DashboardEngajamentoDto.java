package br.com.lunix.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

// Dto para pegar as informações de engajamento para o dashboard do admin
public record DashboardEngajamentoDto(
        @Schema(description = "Total de avaliações feitas no sistema", example = "500")
        long totalAvaliacoes,
        @Schema(description = "Média de todas as notas do sistema", example = "8.6")
        double mediaGlobalNotas
) {
}
