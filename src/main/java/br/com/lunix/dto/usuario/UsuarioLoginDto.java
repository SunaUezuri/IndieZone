package br.com.lunix.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
   DTO utilizado para o formulário de login.

   @NotBlank - Mostra que o campo não pode estar vazio.
   @Email - Indica maneira com que o email deve ser formatado.
*/
public record UsuarioLoginDto(
        @Schema(description = "E-mail cadastrado", example = "admin@lunix.com")
        @NotBlank(message = "O campo email deve ser preenchido")
        @Email(message = "Por favor insira um email válido")
        String email,

        @Schema(description = "Senha do usuário", example = "12345678")
        @NotBlank(message = "A senha não pode ser vazia")
        String senha
) {
}
