package br.com.lunix.aggregation;

import br.com.lunix.dto.avaliacao.ResultadoAgregacaoDto;

// Interface para declarar os métodos a serem feitos pelo próprio banco de dados
public interface AvaliacaoRepositoryCustom {
    ResultadoAgregacaoDto calcularMediaDoJogo(String jogoId);
    Double calcularMediaGlobal();
}
