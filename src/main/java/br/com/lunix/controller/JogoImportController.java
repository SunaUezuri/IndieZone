package br.com.lunix.controller;

import br.com.lunix.dto.error.StandardError;
import br.com.lunix.dto.jogos.JogoMapeadoDto;
import br.com.lunix.services.jogo.JogoImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jogos/import")
@SecurityRequirement(name = "bearerAuth")// Rota base para importações
@Tag(name = "Integrações / Importação", description = "Ferramentas para buscar metadados de APIs externas.")
@RequiredArgsConstructor
public class JogoImportController {

    private final JogoImportService importService;

    @GetMapping("/rawg")
    @Operation(summary = "Importar do RAWG", description = "Busca metadados na API da RAWG pelo título para preenchimento automático.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados encontrados e mapeados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoMapeadoDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado na base externa",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoMapeadoDto> importarDadosRawg(
            @Parameter(description = "Título do jogo", example = "Hollow Knight") @RequestParam String titulo
    ) {
        return ResponseEntity.ok(importService.importarDadosRawg(titulo));
    }
}
