package br.com.lunix.controller;

import br.com.lunix.dto.empresa.EmpresaDetalhesDto;
import br.com.lunix.dto.empresa.EmpresaRequestDto;
import br.com.lunix.dto.empresa.EmpresaResponseDto;
import br.com.lunix.dto.empresa.EmpresaUpdateDto;
import br.com.lunix.dto.error.StandardError;
import br.com.lunix.services.empresa.EmpresaService;
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

@RestController
@RequestMapping("/empresas")
@Tag(name = "Empresas", description = "Endpoints para gestão de estúdios e empresas desenvolvedoras.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService service;

    @GetMapping
    @Operation(summary = "Listar Todas", description = "Retorna uma lista paginada de todas as empresas cadastradas.")
    public ResponseEntity<Page<EmpresaResponseDto>> listarTodas(
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes da Empresa", description = "Retorna dados da empresa e a lista de jogos desenvolvidos por ela.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpresaDetalhesDto.class))),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<EmpresaDetalhesDto> buscarPorId(
            @Parameter(description = "ID da empresa", example = "650c...") @PathVariable String id
    ) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/pais")
    @Operation(summary = "Filtrar por País", description = "Busca empresas filtrando pelo país de origem.")
    public ResponseEntity<Page<EmpresaResponseDto>> buscarPorPais(
            @Parameter(description = "Nome do país", example = "Brasil") @RequestParam String nome,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findByPais(nome, page, size));
    }

    @GetMapping("/nome")
    @Operation(summary = "Filtrar por Nome", description = "Busca empresas filtrando pelo nome.")
    public ResponseEntity<Page<EmpresaResponseDto>> buscarPorNome(
            @Parameter(description = "Nome da empresa", example = "Team Cherry") @RequestParam String nome,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findByNome(nome, page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar Empresa (Admin)", description = "Registra um novo estúdio/empresa na plataforma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpresaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou nome duplicado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<EmpresaResponseDto> cadastrar(@RequestBody @Valid EmpresaRequestDto dto) {
        EmpresaResponseDto novaEmpresa = service.create(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(novaEmpresa.id()).toUri();
        return ResponseEntity.created(uri).body(novaEmpresa);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar Empresa (Admin)", description = "Atualiza dados cadastrais da empresa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<EmpresaResponseDto> atualizar(
            @Parameter(description = "ID da empresa", example = "650c...") @PathVariable String id,
            @RequestBody @Valid EmpresaUpdateDto dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar Empresa (Admin)", description = "Remove o registro de uma empresa do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da empresa", example = "650c...") @PathVariable String id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}