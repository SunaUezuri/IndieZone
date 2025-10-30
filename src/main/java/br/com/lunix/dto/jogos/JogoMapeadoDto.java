package br.com.lunix.dto.jogos;

import br.com.lunix.model.entities.Jogo;

public record JogoMapeadoDto(
        Jogo jogo,
        String nomeDesenvolvedorPrincipal
) {
}
