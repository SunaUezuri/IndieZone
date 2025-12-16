package br.com.lunix.controller;

import br.com.lunix.dto.error.StandardError;
import br.com.lunix.dto.usuario.TokenResponseDto;
import br.com.lunix.dto.usuario.UsuarioLoginDto;
import br.com.lunix.dto.usuario.UsuarioProfileDto;
import br.com.lunix.dto.usuario.UsuarioRegistroDto;
import br.com.lunix.services.usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints públicos responsáveis pelo registro de novos usuários e login.")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService service;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica um usuário utilizando e-mail e senha. Retorna um Token JWT se as credenciais forem válidas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Conta desativada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid UsuarioLoginDto dto) {
        TokenResponseDto token = service.login(dto);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar um novo usuário", description = "Cria uma nova conta de usuário com perfil padrão. Verifica se o e-mail já existe no banco antes de salvar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioProfileDto.class))),
            @ApiResponse(responseCode = "422", description = "Erro de validação",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "400", description = "Violação de regra de negócio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<UsuarioProfileDto> registrar(@RequestBody @Valid UsuarioRegistroDto dto) {
        UsuarioProfileDto novoUsuario = service.registrar(dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/usuarios/{id}")
                .buildAndExpand(novoUsuario.id())
                .toUri();

        return ResponseEntity.created(uri).body(novoUsuario);
    }

}
