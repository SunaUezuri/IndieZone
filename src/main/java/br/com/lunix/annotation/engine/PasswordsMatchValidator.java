package br.com.lunix.annotation.engine;

import br.com.lunix.annotation.interfaces.PasswordsMatch;
import br.com.lunix.dto.usuario.UsuarioRegistroDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, UsuarioRegistroDto> {

    @Override
    public boolean isValid(UsuarioRegistroDto dto, ConstraintValidatorContext context) {
        if (dto.senha() == null || dto.confirmacaoSenha() == null) {
            return true;
        }

        // A lógica principal: compara os dois campos.
        return dto.senha().equals(dto.confirmacaoSenha());
    }
}