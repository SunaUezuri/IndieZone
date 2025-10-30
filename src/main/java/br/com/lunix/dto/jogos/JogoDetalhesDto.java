package br.com.lunix.dto.jogos;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;

import java.time.LocalDate;
import java.util.List;

// Record para exibir os detalhes sobre um jogo.
public record JogoDetalhesDto(
        String id,
        String titulo,
        String descricao,
        String urlCapa,
        LocalDate dataLancamento,
        ClassificacaoIndicativa classificacao,
        List<Genero> generos,
        List<Plataforma> plataformas,
        double notaMedia,
        int totalAvaliacoes,

        String nomeCriador,

        List<AvaliacaoResponseDto> avaliacoesAdmin,
        List<AvaliacaoResponseDto> avaliacoesUsuario,
        List<AvaliacaoResponseDto> avaliacoesDev
) {
}
