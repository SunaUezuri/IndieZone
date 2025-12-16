package br.com.lunix.controller;

import br.com.lunix.dto.error.StandardError;
import br.com.lunix.dto.usuario.UsuarioAdminListDto;
import br.com.lunix.dto.usuario.UsuarioProfileDto;
import br.com.lunix.dto.usuario.UsuarioRolePatchDto;
import br.com.lunix.dto.usuario.UsuarioUpdateDto;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.services.usuario.UsuarioService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gestão de perfil e administração de usuários.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos (Admin)", description = "Retorna uma lista paginada de todos os usuários cadastrados. Exclusivo para Administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Usuário não é ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<UsuarioAdminListDto>> listarTodos(
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.listarTodos(page, size));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar por nome (Admin)", description = "Realiza uma busca parcial pelo nome do usuário. Útil para filtros em Dashboards.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Usuário não é ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Page<UsuarioAdminListDto>> buscarPorNome(
            @Parameter(description = "Trecho do nome a ser buscado", example = "Jean") @RequestParam String nome,
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.buscarPorNome(nome, page, size));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar usuário (Admin)", description = "Atualiza dados cadastrais, status (ativo/inativo) e vínculo com empresa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioAdminListDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou Empresa não encontrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos campos enviados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<UsuarioAdminListDto> atualizar(
            @Parameter(description = "ID do usuário a ser atualizado", example = "650c...") @PathVariable String id,
            @RequestBody @Valid UsuarioUpdateDto dto
    ) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar permissões (Admin)", description = "Promove ou rebaixa usuários alterando suas Roles (Ex: USER -> DEV).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissões atualizadas com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioAdminListDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<UsuarioAdminListDto> atualizarRoles(
            @Parameter(description = "ID do usuário", example = "650c...") @PathVariable String id,
            @RequestBody @Valid UsuarioRolePatchDto dto
    ) {
        return ResponseEntity.ok(service.atualizarRoles(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar conta (Admin)", description = "Realiza a exclusão lógica (Soft Delete), definindo o status do usuário como inativo. Não apaga os dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso (Sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Void> desativar(
            @Parameter(description = "ID do usuário a ser desativado", example = "650c...") @PathVariable String id
    ) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Meu Perfil", description = "Retorna os dados detalhados do usuário atualmente logado (identificado pelo Token).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioProfileDto.class))),
            @ApiResponse(responseCode = "403", description = "Token inválido ou conta desativada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<UsuarioProfileDto> meuPerfil(Authentication authentication) {
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(service.buscarPorId(usuarioLogado.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Busca o perfil de um usuário específico pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<UsuarioProfileDto> buscarPorId(
            @Parameter(description = "ID do usuário", example = "650c...") @PathVariable String id
    ) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}
