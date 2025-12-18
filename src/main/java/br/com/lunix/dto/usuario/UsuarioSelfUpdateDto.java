package br.com.lunix.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioSelfUpdateDto(
        @Schema(description = "Novo nome do usuário", example = "João da Silva")
        @Size(min = 3, message = "O nome deve ter no mínimo 3 caracteres")
        String nome,

        @Schema(description = "Novo e-mail", example = "joao@email.com")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Schema(description = "Nova senha (opcional). Envie apenas se quiser alterar.", example = "123456")
        @Size(min = 8, message = "A senha deve ter no mínimo 6 caracteres")
        String senha
) {
}
