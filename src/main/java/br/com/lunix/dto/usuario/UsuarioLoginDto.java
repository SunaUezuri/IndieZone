package br.com.lunix.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginDto(
        @NotBlank(message = "O campo email deve ser preenchido")
        @Email(message = "Por favor insira um email válido")
        String email,
        @NotBlank(message = "A senha não pode ser vazia")
        String senha
) {
}
