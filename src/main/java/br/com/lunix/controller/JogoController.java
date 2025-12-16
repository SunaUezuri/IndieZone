package br.com.lunix.controller;

import br.com.lunix.dto.error.StandardError;
import br.com.lunix.dto.jogos.*;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.services.jogo.JogoImportService;
import br.com.lunix.services.jogo.JogoPrecoService;
import br.com.lunix.services.jogo.JogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/jogos")
@Tag(name = "Jogos", description = "Endpoints para gestão, busca e vitrine de jogos indie.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class JogoController {

    private final JogoService jogoService;

    private final JogoPrecoService precoService;

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes do Jogo", description = "Retorna dados completos, incluindo avaliações separadas por perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoDetalhesDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoDetalhesDto> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(jogoService.buscarDetalhesPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar Todos", description = "Lista paginada ordenada por data de lançamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogos encontrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum jogo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jogoService.listarTodos(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar por Título", description = "Pesquisa jogos pelo nome (busca parcial case-insensitive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> buscarPorTitulo(
            @RequestParam String termo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jogoService.buscarPorTitulo(termo, page, size));
    }

    @GetMapping("/genero/{genero}")
    @Operation(summary = "Filtrar por Gênero")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> buscarPorGenero(
            @PathVariable Genero genero,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jogoService.buscarPorGenero(genero, page, size));
    }

    @GetMapping("/plataforma/{plataforma}")
    @Operation(summary = "Filtrar por Plataforma")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> buscarPorPlataforma(
            @PathVariable Plataforma plataforma,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jogoService.buscarPorPlataforma(plataforma, page, size));
    }

    @GetMapping("/empresa/{id}")
    @Operation(summary = "Filtrar por Empresa")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> buscarPorEmpresa(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jogoService.buscarPorEmpresa(id, page, size));
    }

    @GetMapping("/dev/{id}")
    @Operation(summary = "Filtrar por Desenvolvedor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Desenvolvedor não encontrado", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> buscarPorDev(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jogoService.buscarPorDev(id, page, size));
    }

    @GetMapping("/top-avaliados")
    @Operation(summary = "Top 10 Melhores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogos não encontrados", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<List<JogoResponseDto>> topAvaliados() {
        return ResponseEntity.ok(jogoService.buscarTop10MelhoresAvaliados());
    }

    @GetMapping("/lancamentos")
    @Operation(summary = "Lançamentos Recentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogos não encontrados", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<List<JogoResponseDto>> topLancamentos() {
        return ResponseEntity.ok(jogoService.buscarTop10Lancamentos());
    }

    @PostMapping
    @Operation(summary = "Cadastrar Jogo", description = "Cadastra um novo jogo. Admins podem definir o dono.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoResponseDto> cadastrar(@RequestBody @Valid JogoAdminRequestDto dto) {
        JogoResponseDto novoJogo = jogoService.cadastrar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(novoJogo.id()).toUri();
        return ResponseEntity.created(uri).body(novoJogo);
    }

    @GetMapping("/meus-jogos")
    @Operation(summary = "Meus Jogos (Área do Dev)", description = "Lista jogos do usuário logado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogos encontrados", content = @Content(schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogos não encontrados", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer acesso de DEV)")
    })
    public ResponseEntity<Page<JogoResponseDto>> listarMeusJogos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jogoService.listarMeusJogos(page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Jogo", description = "Requer permissão de Dono ou Admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoResponseDto> atualizar(
            @PathVariable String id,
            @RequestBody @Valid JogoUpdateDto dto
    ) {
        return ResponseEntity.ok(jogoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/generos")
    @Operation(summary = "Atualizar Gêneros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Void> atualizarGeneros(
            @PathVariable String id,
            @RequestBody @Valid JogoGenresPatchDto dto
    ) {
        jogoService.patchGeneros(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Jogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        jogoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync-prices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sincronização Global de Preços (Admin)", description = "Dispara atualização de todos os jogos via RabbitMQ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Processamento iniciado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Void> syncPrices() {
        precoService.solicitarAtualizacaoGlobalAdmin();
        return ResponseEntity.accepted().build();
    }
}