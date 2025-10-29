package br.com.lunix.annotation.engine;

import br.com.lunix.annotation.interfaces.PastOrPresentDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/*
    Classe responsável por realizar a validação de datas
    garantindo que uma data futura não está sendo inserida.

    Implementa a interface **ConstraintValidator** para criar
    o método de validação utilizando o @PastOrPresentDate.
*/
public class PastOrPresentDateValidator implements ConstraintValidator<PastOrPresentDate, LocalDate> {

    /*
        Método responsável por validar a data garantindo que
        ela não seja posterior a atual.

        @param date - Data que será validada

        return: Retorna um boolean indicando se a data passou na validação ou não
    */
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        // Se a data for fazia automaticamente é invalidada
        if (date == null) {
            return true;
        }

        // Se a data for posterior a atual ela é invalidada
        return !date.isAfter(LocalDate.now());
    }
}
