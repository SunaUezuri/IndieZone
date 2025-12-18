package br.com.lunix.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

// Dto para as informações de usuário no dashboard
public record DashboardUsuariosDto(
        @Schema(description = "Total de usuários da aplicação", example = "120")
        long totalUsuarios,
        @Schema(description = "Total de usuários registrados no último mês", example = "23")
        long novosUsuariosUltimoMes,
        @Schema(description = "Map de role dos usuários", example = "{ ROLE_DEV: 12,... }")
        Map<String, Long> distribuicaoPorRole
) {
}
