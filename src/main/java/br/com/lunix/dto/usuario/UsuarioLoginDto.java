package br.com.lunix.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginDto(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String senha
) {
}
