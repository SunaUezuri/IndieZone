package br.com.lunix.controller;

import br.com.lunix.dto.dashboard.DashboardJogoDto;
import br.com.lunix.dto.error.StandardError;
import br.com.lunix.services.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "Dashboard", description = "Dados analíticos e estatísticos para administração.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/jogos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Métricas de Jogos", description = "Retorna contagem total, jogos sem preço e distribuição por gênero.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardJogoDto.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<DashboardJogoDto> getDadosDashboard() {
        return ResponseEntity.ok(dashboardService.gerarDadosDashboard());
    }
}