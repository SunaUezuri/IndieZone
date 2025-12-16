package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.Genero;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/*
    Record usado para realizar um PATCH dos gêneros
    de um jogo.

    @NotBlank - Garante que o campo não seja nulo.
*/
public record JogoGenresPatchDto(
        @NotEmpty(message = "O jogo deve ter ao menos um genero")
        @Schema(description = "Lista com os gêneros do jogo", example = "[ 'PLATAFORMA', 'ACAO' ]")
        List<Genero> generos) {
}
