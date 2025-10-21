package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.Genero;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record JogoGenresPatchDto(@NotEmpty List<Genero> generos) {
}
