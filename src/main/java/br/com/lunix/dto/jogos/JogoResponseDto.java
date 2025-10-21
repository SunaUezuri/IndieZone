package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.ClassificacaoIndicativa;

public record JogoResponseDto(
        String id,
        String titulo,
        String urlCapa,
        String nomeCriador,
        double notaMedia,
        ClassificacaoIndicativa classificacao
) {
}
