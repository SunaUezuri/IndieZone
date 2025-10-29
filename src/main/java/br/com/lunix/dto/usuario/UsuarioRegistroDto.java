package br.com.lunix.dto.usuario;

import br.com.lunix.annotation.interfaces.PasswordsMatch;
import br.com.lunix.annotation.interfaces.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
   DTO utilizado para o cadastro de usuários
   na aplicação.

   @PasswordsMatch - Garante que o campo de senha e confirmação de senha sejam iguais
   @NotBlank - Indica que o campo não deve estar vazio.
   @Email - Define o formato que deve ser utilizado no email.
   @UniqueEmail - Garante que o email já não esteja registrado no banco.
   @Size - Define o tamanho mínimo a se ter em um campo.
*/
@PasswordsMatch
public record UsuarioRegistroDto(
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Por favor, insira um e-mail válido.")
        @UniqueEmail
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        String senha,

        @NotBlank(message = "A confirmação de senha é obrigatória.")
        String confirmacaoSenha,
        String idEmpresa
) {
}
