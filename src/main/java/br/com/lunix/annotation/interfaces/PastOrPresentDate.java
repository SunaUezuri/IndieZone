package br.com.lunix.annotation.interfaces;

import br.com.lunix.annotation.engine.PastOrPresentDateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Anotação utilizada para definir que uma data de lançamento de jogo
    não pode ser em uma data futura.
*/
@Constraint(validatedBy = PastOrPresentDateValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PastOrPresentDate {
    String message() default "A data não pode ser no futuro.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
