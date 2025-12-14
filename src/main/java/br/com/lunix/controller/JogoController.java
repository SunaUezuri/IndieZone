package br.com.lunix.controller;

import br.com.lunix.dto.dashboard.DashboardJogoDto;
import br.com.lunix.dto.error.StandardError;
import br.com.lunix.dto.jogos.*;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.services.JogoService;
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

    private final JogoService service;


    @GetMapping("/{id}")
    @Operation(summary = "Detalhes do Jogo", description = "Retorna dados completos, incluindo avaliações separadas por perfil (Admin, Dev, User).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoDetalhesDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoDetalhesDto> buscarPorId(
            @Parameter(description = "ID do jogo", example = "650c...") @PathVariable String id
    ) {
        return ResponseEntity.ok(service.buscarDetalhesPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar Todos", description = "Retorna a lista completa de jogos paginada, ordenada por data de lançamento.")
    public ResponseEntity<Page<JogoResponseDto>> listarTodos(
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.listarTodos(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar por Título", description = "Pesquisa jogos pelo nome (busca parcial case-insensitive).")
    public ResponseEntity<Page<JogoResponseDto>> buscarPorTitulo(
            @Parameter(description = "Trecho do título", example = "Hollow") @RequestParam String termo,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.buscarPorTitulo(termo, page, size));
    }

    @GetMapping("/genero/{genero}")
    @Operation(summary = "Filtrar por Gênero", description = "Lista jogos de um gênero específico.")
    public ResponseEntity<Page<JogoResponseDto>> buscarPorGenero(
            @Parameter(description = "Gênero do jogo", example = "RPG") @PathVariable Genero genero,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.buscarPorGenero(genero, page, size));
    }

    @GetMapping("/plataforma/{plataforma}")
    @Operation(summary = "Filtrar por Plataforma", description = "Lista jogos disponíveis em uma plataforma específica.")
    public ResponseEntity<Page<JogoResponseDto>> buscarPorPlataforma(
            @Parameter(description = "Plataforma desejada", example = "PC") @PathVariable Plataforma plataforma,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.buscarPorPlataforma(plataforma, page, size));
    }

    @GetMapping("/empresa/{id}")
    @Operation(summary = "Filtrar por Empresa", description = "Lista todos os jogos publicados por uma empresa específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> buscarPorEmpresa(
            @Parameter(description = "ID da empresa", example = "650c...") @PathVariable String id,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.buscarPorEmpresa(id, page, size));
    }

    @GetMapping("/dev/{id}")
    @Operation(summary = "Filtrar por Desenvolvedor", description = "Lista todos os jogos publicados por um desenvolvedor autônomo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Desenvolvedor não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<JogoResponseDto>> buscarPorDev(
            @Parameter(description = "ID do desenvolvedor", example = "650c...") @PathVariable String id,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.buscarPorDev(id, page, size));
    }

    @GetMapping("/top-avaliados")
    @Operation(summary = "Top 10 Melhores", description = "Ranking dos 10 jogos com maiores notas médias na plataforma.")
    public ResponseEntity<List<JogoResponseDto>> topAvaliados() {
        return ResponseEntity.ok(service.buscarTop10MelhoresAvaliados());
    }

    @GetMapping("/lancamentos")
    @Operation(summary = "Lançamentos Recentes", description = "Os 10 jogos adicionados ou lançados mais recentemente.")
    public ResponseEntity<List<JogoResponseDto>> topLancamentos() {
        return ResponseEntity.ok(service.buscarTop10Lancamentos());
    }

    @PostMapping
    @Operation(summary = "Cadastrar Jogo", description = "Cadastra um novo jogo. Devs cadastram para si mesmos/empresa. Admins podem definir o dono.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Jogo criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou regra de negócio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoResponseDto> cadastrar(@RequestBody @Valid JogoAdminRequestDto dto) {
        JogoResponseDto novoJogo = service.cadastrar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(novoJogo.id()).toUri();
        return ResponseEntity.created(uri).body(novoJogo);
    }

    @GetMapping("/meus-jogos")
    @Operation(summary = "Meus Jogos (Área do Dev)", description = "Lista apenas os jogos que o usuário logado (ou sua empresa) criou. Se for Admin, lista todos.")
    public ResponseEntity<Page<JogoResponseDto>> listarMeusJogos(
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.listarMeusJogos(page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Jogo", description = "Atualiza dados básicos. Apenas o Dono ou Admin podem fazer isso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para alterar este jogo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoResponseDto> atualizar(
            @Parameter(description = "ID do jogo", example = "650c...") @PathVariable String id,
            @RequestBody @Valid JogoUpdateDto dto
    ) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PatchMapping("/{id}/generos")
    @Operation(summary = "Atualizar Gêneros", description = "Endpoint específico para atualizar apenas a lista de gêneros de um jogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Gêneros atualizados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Void> atualizarGeneros(
            @Parameter(description = "ID do jogo", example = "650c...") @PathVariable String id,
            @RequestBody @Valid JogoGenresPatchDto dto
    ) {
        service.patchGeneros(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Jogo", description = "Remove um jogo permanentemente. Requer permissão de Dono ou Admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Jogo deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para deletar este jogo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do jogo", example = "650c...") @PathVariable String id
    ) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dashboard (Admin)", description = "Estatísticas gerais da plataforma para painel administrativo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<DashboardJogoDto> dashboard() {
        return ResponseEntity.ok(service.gerarDadosDashboard());
    }

    @PostMapping("/sync-prices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sincronização Global de Preços", description = "Dispara evento assíncrono para atualizar preços de TODOS os jogos via API externa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação aceita e em processamento em background"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Requer ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Void> syncPrices() {
        service.solicitarAtualizacaoGlobal();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/rawg-import")
    @Operation(summary = "Importar Metadados (RAWG)", description = "Busca dados na API externa para preenchimento automático de formulário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados encontrados e mapeados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JogoMapeadoDto.class))),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado na base externa ou não é Indie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<JogoMapeadoDto> importarDadosRawg(
            @Parameter(description = "Título do jogo para pesquisa", example = "Hollow Knight") @RequestParam String titulo
    ) {
        return ResponseEntity.ok(service.importarDadosRawg(titulo));
    }
}