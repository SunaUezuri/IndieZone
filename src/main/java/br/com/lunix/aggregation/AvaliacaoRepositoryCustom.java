package br.com.lunix.aggregation;

import br.com.lunix.dto.avaliacao.ResultadoAgregacaoDto;

// Interface para declarar o método de calcular média
public interface AvaliacaoRepositoryCustom {
    ResultadoAgregacaoDto calcularMediaDoJogo(String jogoId);
}
