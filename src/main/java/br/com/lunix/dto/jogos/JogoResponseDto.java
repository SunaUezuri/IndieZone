package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.ClassificacaoIndicativa;

import java.io.Serializable;

// DTO de resposta para os jogos
public record JogoResponseDto(
        String id,
        String titulo,
        String urlCapa,
        String nomeCriador,
        double notaMedia,
        ClassificacaoIndicativa classificacao
) implements Serializable {
}
