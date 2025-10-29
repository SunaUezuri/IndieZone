package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.Genero;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/*
    Record usado para realizar um PATCH dos gêneros
    de um jogo.

    @NotBlank - Garante que o campo não seja nulo.
*/
public record JogoGenresPatchDto(@NotEmpty(message = "O jogo deve ter ao menos um genero") List<Genero> generos) {
}
