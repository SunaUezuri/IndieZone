package br.com.lunix.dto;

import br.com.lunix.annotation.engine.UniqueEmailValidator;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "mongock.enabled=false")
public class UniqueEmailValidatorTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UniqueEmailValidator validator;

    @Test
    public void quandoEmailNaoExisteDeveSerValido() {
        String email = "novo@email.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        boolean isValid = validator.isValid(email, null);

        assertThat(isValid).isTrue();
    }

    @Test
    public void quandoEmailJaExisteDeveSerInvalido() {
        String email = "existente@email.com";
        when(repository.findByEmail(email)).thenReturn(Optional.of(new Usuario()));

        boolean isValid = validator.isValid(email, null);

        assertThat(isValid).isFalse();
    }

}
