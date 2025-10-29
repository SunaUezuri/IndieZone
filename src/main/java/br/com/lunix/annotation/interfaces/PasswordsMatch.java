package br.com.lunix.annotation.interfaces;

import br.com.lunix.annotation.engine.PasswordsMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Anotação criada para verificar se os campos de 'senha' e 'confirme a senha'
    tem exatamente o mesmo valor.
*/
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {

    String message() default "As senhas não coincidem.";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
