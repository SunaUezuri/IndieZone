package br.com.lunix.dto;

import br.com.lunix.dto.jogos.JogoRequestDto;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/*
    Classe de teste para validar as anotações
    de validação da classe do DTO de jogo.
*/
public class JogoRequestDtoTest {

    @Autowired
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Inicializa o validador padrão do Hibernate/Jakarta
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /*
        Valida se o jogo está com a data no passado e se
        não há violações.
    */
    @Test
    public void quandoDtoEValidoComDataNoPassadoNaoDeveHaverViolacoes() {
        // Cria um exemplo de dto
        var dto = new JogoRequestDto(
                "Jogo Válido",
                "Descrição OK",
                null,
                LocalDate.now().minusYears(1),
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA),
                List.of(Plataforma.PC),
                null,
                null
        );

        // Aplica o validador no dto de exemplo
        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        // Garante que não há violações
        assertThat(violations).isEmpty();
    }

    /*
        Teste para garantir que não há violações ao inserir
        um jogo com a data atual.
    */
    @Test
    public void quandoDtoEValidoComDataNoPresenteNaoDeveHaverViolacoes() {
        // Cria um exemplo de dto
        var dto = new JogoRequestDto(
                "Jogo Válido",
                "Descrição OK",
                null,
                LocalDate.now(), // Data atual
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA),
                List.of(Plataforma.PC),
                null,
                null
        );

        // Aplica o validador
        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        // Garante que não há violações
        assertThat(violations).isEmpty();
    }

    /*
        Teste para garantir que ao inserir um jogo com
        data no futuro ele tenha uma violação.
    */
    @Test
    public void quandoDataDeLancamentoENoFuturoDeveHaverViolacao() {
        var dto = new JogoRequestDto(
                "Jogo Inválido",
                "Descrição OK",
                null,
                LocalDate.now().plusDays(1), // Data no futuro
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA),
                List.of(Plataforma.PC),
                null,
                null
        );

        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        // Garante que há exatamente uma violação
        assertThat(violations).hasSize(1);
        ConstraintViolation<JogoRequestDto> violation = violations.iterator().next();

        // Garante que o campo da violação seja o de dataLancamento
        assertThat(violation.getPropertyPath().toString()).isEqualTo("dataLancamento");

        // Garante que o texto de erro seja exatamente o definido
        assertThat(violation.getMessage()).isEqualTo("A data não pode ser no futuro.");
    }

    /*
        Teste para garantir que a data de lançamento
        não esteja vazia.
    */
    @Test
    public void quandoDataDeLancamentoENulaDeveHaverViolacaoDeNotNull() {
        var dto = new JogoRequestDto(
                "Jogo Inválido",
                "Descrição OK",
                null,
                null, // Data nula
                ClassificacaoIndicativa.LIVRE,
                List.of(Genero.AVENTURA),
                List.of(Plataforma.PC),
                null,
                null
        );

        Set<ConstraintViolation<JogoRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<JogoRequestDto> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("A data de lançamento é obrigatória");
    }
}