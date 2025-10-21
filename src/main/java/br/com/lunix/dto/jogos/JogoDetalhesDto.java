package br.com.lunix.dto.jogos;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.model.entities.Jogo;

import java.util.List;

public record JogoDetalhesDto(
        Jogo jogo,
        List<AvaliacaoResponseDto> avaliacaoAdmin,
        List<AvaliacaoResponseDto> avaliacaoUsuario,
        List<AvaliacaoResponseDto> avaliacaoDev
) {
}
