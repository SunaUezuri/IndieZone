package br.com.lunix.aggregation.impl;

import br.com.lunix.aggregation.interfaces.AvaliacaoRepositoryCustom;
import br.com.lunix.dto.avaliacao.ResultadoAgregacaoDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

/*
    Classe de implementação para fazer uma consulta direto no banco
    para realizar o cálculo da nota média de um jogo
*/
@RequiredArgsConstructor
public class AvaliacaoRepositoryImpl implements AvaliacaoRepositoryCustom {

    private final MongoTemplate mongoTemplate;


    @Override
    public ResultadoAgregacaoDto calcularMediaDoJogo(String jogoId) {
        try {
            // Primeiro filtra as avaliações de um jogo pelo ID
            var matchOperation = Aggregation.match(Criteria.where("jogo").is(new ObjectId(jogoId)));

            // Agrupa pelo campo 'jogo' e calcula a média e contagem
            var groupOperation = Aggregation.group("jogo")
                    .avg("nota").as("mediaCalculada")
                    .count().as("totalAvaliacoes");

            // Executa a query
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);

            // Coleta os resultados
            AggregationResults<ResultadoAgregacaoDto> resultadoAgregacao = mongoTemplate
                    .aggregate(aggregation, "avaliacoes", ResultadoAgregacaoDto.class);

            return resultadoAgregacao.getUniqueMappedResult();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
