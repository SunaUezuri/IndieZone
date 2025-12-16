package br.com.lunix.dto.jogos;

import br.com.lunix.model.entities.Jogo;
import io.swagger.v3.oas.annotations.media.Schema;

/*
    DTO responsável por receber o objeto mapeado
    pelo RawgMapper.
*/
public record JogoMapeadoDto(
        @Schema(description = "Jogo encontrado")
        Jogo jogo,
        @Schema(description = "Nome do desenvolvedor", example = "Team Cherry")
        String nomeDesenvolvedorPrincipal
) {
}
