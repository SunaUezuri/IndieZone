package br.com.lunix.annotation.engine;

import br.com.lunix.annotation.interfaces.UniqueEmail;
import br.com.lunix.repository.UsuarioRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/*
    Componente resposável por garantir que o email inserido
    seja único.
*/
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    // Utiliza o repositório de Usuario para buscar o usuário pelo email
    @Autowired
    private UsuarioRepository usuarioRepository;

    /*
        Realiza a validação do email

        @param email - Email a ser validado

        return: boolean indicando se o email é válido ou não
    */
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Se o email estiver vazio automaticamente falha na validação
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        // Se ele encontrar um email no banco igual ao inserido falha na validação
        return !usuarioRepository.findByEmail(email).isPresent();
    }
}
