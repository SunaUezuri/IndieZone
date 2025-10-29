package br.com.lunix.annotation.interfaces;

import br.com.lunix.annotation.engine.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Anotação para garantir que o email inserido
    seja único.
*/
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "Este e-mail já está em uso.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
