package br.com.lunix.dto.jogos;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.model.entities.Jogo;

import java.util.List;

// Record para exibir os detalhes sobre um jogo.
public record JogoDetalhesDto(
        Jogo jogo,
        List<AvaliacaoResponseDto> avaliacaoAdmin,
        List<AvaliacaoResponseDto> avaliacaoUsuario,
        List<AvaliacaoResponseDto> avaliacaoDev
) {
}
