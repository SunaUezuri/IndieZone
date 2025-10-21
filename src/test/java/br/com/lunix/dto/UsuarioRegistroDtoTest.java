package br.com.lunix.dto;

import br.com.lunix.dto.usuario.UsuarioRegistroDto;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.Validator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UsuarioRegistroDtoTest {

    @Autowired
    private Validator validator;

    @Test
    public void quandoDtoEValidoNaoDeveHaverViolacoes() {
        var dto = new UsuarioRegistroDto("Nome válido", "teste@valido.com",
                "senha123", "senha123", null);

        Set<ConstraintViolation<UsuarioRegistroDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    public void quandoCampoObrigatoriosEstaoEmBrancoDeveHaverViolacoes() {
        var dto = new UsuarioRegistroDto("", "", "", "", null);

        Set<ConstraintViolation<UsuarioRegistroDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(5);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "O nome é obrigatório.",
                        "O e-mail é obrigatório.",
                        "A senha é obrigatória.",
                        "A confirmação de senha é obrigatória.",
                        "A senha deve ter no mínimo 8 caracteres."
                );
    }

    @Test
    public void quandoSenhasNaoCoincidemDeveHaverViolacaoDePasswordsMatch() {
        var dto = new UsuarioRegistroDto("Nome Válido", "teste@valido.com",
                "senha1234", "senhaDIFERENTE", null);

        Set<ConstraintViolation<UsuarioRegistroDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("As senhas não coincidem.");
    }
}
