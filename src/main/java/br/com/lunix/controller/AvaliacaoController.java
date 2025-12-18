package br.com.lunix.controller;

import br.com.lunix.dto.avaliacao.AvaliacaoRequestDto;
import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.avaliacao.AvaliacaoUpdateDto;
import br.com.lunix.dto.error.StandardError;
import br.com.lunix.services.avaliacao.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Avaliações", description = "Gestão de notas e comentários sobre os jogos")
public class AvaliacaoController {

    private final AvaliacaoService service;

    @PostMapping("jogo/{jogoId}")
    @Operation(summary = "Criar avaliação", description = "Adiciona uma nota e comentário a um jogo. Recalcula a média do jogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou Regra de Negócio (ex: já avaliou)",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Erro de permissão/autenticação",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de interesse (Dev avaliando próprio jogo)",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<AvaliacaoResponseDto> criar(
            @PathVariable String jogoId,
            @RequestBody @Valid AvaliacaoRequestDto dto
            ) {
        AvaliacaoResponseDto response = service.criar(jogoId, dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Avaliação", description = "Permite editar nota e comentário. Apenas o autor pode fazer isso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = AvaliacaoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou Regra de Negócio",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Erro de permissão/autenticação",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<AvaliacaoResponseDto> atualizar(
            @PathVariable String id,
            @RequestBody @Valid AvaliacaoUpdateDto dto
            ) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Avaliação", description = "Remove a avaliação. Autor ou Admin podem executar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou Regra de Negócio",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Erro de permissão/autenticação",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Comentários não encontrados",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jogo/{jogoId}")
    @Operation(summary = "Listar por Jogo", description = "Lista todas as avaliações de um jogo específico (paginado).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avaliações encontradas com sucesso",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Comentários não encontrados",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<AvaliacaoResponseDto>> listarPorJogo(
            @PathVariable String jogoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.listarPorJogo(jogoId, page, size));
    }

    @GetMapping("/me")
    @Operation(summary = "Minhas Avaliações", description = "Lista o histórico de avaliações do usuário logado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avaliações encontradas com sucesso",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Erro de permissão/autenticação",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Comentários não encontrados",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<AvaliacaoResponseDto>> listarMinhas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.listarMinhas(page, size));
    }
}
