package br.com.lunix.dto.jogos;

import br.com.lunix.model.entities.Jogo;

/*
    DTO responsável por receber o objeto mapeado
    pelo RawgMapper.
*/
public record JogoMapeadoDto(
        Jogo jogo,
        String nomeDesenvolvedorPrincipal
) {
}
