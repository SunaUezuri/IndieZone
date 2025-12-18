package br.com.lunix.dto.dashboard;

import br.com.lunix.dto.jogos.JogoResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

// DTO com informações pricipais sobre os jogos
public record DashboardConteudoDto(
        @Schema(description = "Total de jogos registrados no sistema", example = "50")
        long totalJogos,
        @Schema(description = "Total de jogos que não tem preços", example = "10")
        long totalJogosSemPreco,
        @Schema(description = "Total de empresas registradas no sistema", example = "45")
        long totalEmpresas,
        @Schema(description = "Total de desenvolvedores registrados no sistema", example = "5")
        long totalDevsAutonomos,
        @Schema(description = "Jogos filtrados pelo gênero", example = "{ 'RPG': 10,... }")
        Map<String, Long> jogosPorGenero,
        @Schema(description = "Jogos filtrados por plataforma", example = "{ 'PC': 50,... }")
        Map<String, Long> jogosPorPlataforma,
        List<JogoResponseDto> top5MelhoresJogos
) {
}
