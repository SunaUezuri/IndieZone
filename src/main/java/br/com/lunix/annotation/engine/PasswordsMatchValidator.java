package br.com.lunix.annotation.engine;

import br.com.lunix.annotation.interfaces.PasswordsMatch;
import br.com.lunix.dto.usuario.UsuarioRegistroDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/*
    Classe responsável por comparar os campos de senha e
    confirmacaoSenha tem valores iguais.

    Implementa a interface **ConstraintValidator** para criar
    o método de validação utilizando o @PasswordsMatch.
*/
public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, UsuarioRegistroDto> {

    /*
        Método responsável por comparar os campos especificados
        e garantir que eles coincidem.

        @param dto - usuário que está sendo cadastrado
        @param context - Contexto do referente a comparação
    */
    @Override
    public boolean isValid(UsuarioRegistroDto dto, ConstraintValidatorContext context) {
        /*
            Garante que os campos não estejam vazios e
            depois os compara para garantir igualdade.
        */

        if (dto.senha() == null || dto.confirmacaoSenha() == null) {
            return true;
        }

        // A lógica principal: compara os dois campos.
        return dto.senha().equals(dto.confirmacaoSenha());
    }
}