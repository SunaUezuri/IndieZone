package br.com.lunix.dto;

import br.com.lunix.dto.jogos.JogoRequestDto;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.Validator;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "mongock.enabled=false")
public class JogoRequestDtoTest {

    @Autowired
    private Validator validator;

    @Test
    public void quandoDtoEValidoComDataNoPassadoNaoDeveHaverViolacoes() {
        var dto = new JogoRequestDto(
                "Jogo Válido", "Descrição OK", null,
                LocalDate.now().minusYears(1),
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA)
        );

        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    public void quandoDtoEValidoComDataNoPresenteNaoDeveHaverViolacoes() {
        var dto = new JogoRequestDto(
                "Jogo Válido", "Descrição OK", null,
                LocalDate.now(), // Data de hoje
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA)
        );

        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    public void quandoDataDeLancamentoENoFuturoDeveHaverViolacao() {
        var dto = new JogoRequestDto(
                "Jogo Inválido", "Descrição OK", null,
                LocalDate.now().plusDays(1), // Data no futuro
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA)
        );

        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<JogoRequestDto> violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString()).isEqualTo("dataLancamento");
        assertThat(violation.getMessage()).isEqualTo("A data não pode ser no futuro.");
    }

    @Test
    public void quandoDataDeLancamentoENulaDeveHaverViolacaoDeNotNull() {
        var dto = new JogoRequestDto(
                "Jogo Inválido", "Descrição OK", null,
                null, // Data nula
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA)
        );

        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<JogoRequestDto> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("A data de lançamento é obrigatória");
    }
}
