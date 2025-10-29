package br.com.lunix.dto;

import br.com.lunix.dto.usuario.UsuarioRegistroDto;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.Validator;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/*
    Teste para garantir o funcionamento correto
    das validações feitas no DTO de usuário.
*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "mongock.enabled=false")
public class UsuarioRegistroDtoTest {

    @Autowired
    private Validator validator;

    /*
        Teste para garantir que quando um cadastro
        for válido ele não haver violações.
    */
    @Test
    public void quandoDtoEValidoNaoDeveHaverViolacoes() {
        var dto = new UsuarioRegistroDto("Nome válido", "teste@valido.com",
                "senha123", "senha123", null);

        Set<ConstraintViolation<UsuarioRegistroDto>> violations = validator.validate(dto);

        // Garante que não há violações
        assertThat(violations).isEmpty();
    }

    /*
        Teste para garantir que há violações quando
        campos obrigatórios estão em branco.
    */
    @Test
    public void quandoCampoObrigatoriosEstaoEmBrancoDeveHaverViolacoes() {
        var dto = new UsuarioRegistroDto("", "", "", "", null);

        Set<ConstraintViolation<UsuarioRegistroDto>> violations = validator.validate(dto);

        // Garante que há 5 violações
        assertThat(violations).hasSize(5);

        // Garante que estas sejam as mensagens de erro
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "O nome é obrigatório.",
                        "O e-mail é obrigatório.",
                        "A senha é obrigatória.",
                        "A confirmação de senha é obrigatória.",
                        "A senha deve ter no mínimo 8 caracteres."
                );
    }

    /*
        Teste para garantir o funcionamento da anotação
        de comparação de campos de senha e confirmacaoSenha.
    */
    @Test
    public void quandoSenhasNaoCoincidemDeveHaverViolacaoDePasswordsMatch() {
        var dto = new UsuarioRegistroDto("Nome Válido", "teste@valido.com",
                "senha1234", "senhaDIFERENTE", null);

        Set<ConstraintViolation<UsuarioRegistroDto>> violations = validator.validate(dto);

        // Garante que quando a senha é diferente de confirmacaoSenha tenha uma violacao
        assertThat(violations).hasSize(1);

        // Garante que a mensagem de erro apresentada seja exatamente a inserida.
        assertThat(violations.iterator().next().getMessage()).isEqualTo("As senhas não coincidem.");
    }

    /*
        Teste para garantir que quando as senhas
        coincidem não há violações.
    */
    @Test
    public void quandoSenhasCoincidemNaoDevemHaverViolacoesDePasswordsMatch() {
        var dto = new UsuarioRegistroDto("Nome Válido", "teste@valido.com",
                "senha1234", "senha1234", null);

        Set<ConstraintViolation<UsuarioRegistroDto>> violations = validator.validate(dto);

        // Garante que não há violações.
        assertThat(violations).isEmpty();
    }
}
