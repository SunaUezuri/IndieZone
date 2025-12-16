package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;

import java.io.Serializable;
import java.util.List;

// DTO de resposta para os jogos
public record JogoResponseDto(
        String id,
        String titulo,
        String urlCapa,
        String nomeCriador,
        double notaMedia,
        List<Genero> generos,
        ClassificacaoIndicativa classificacao
) implements Serializable {
}
