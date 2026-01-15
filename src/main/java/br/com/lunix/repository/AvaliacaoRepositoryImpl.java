package br.com.lunix.repository;

import br.com.lunix.aggregation.AvaliacaoRepositoryCustom;
import br.com.lunix.dto.avaliacao.ResultadoAgregacaoDto;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

/*
    Classe de implementação para fazer uma consulta direto no banco
    para realizar o cálculo da nota média de um jogo
*/
@RequiredArgsConstructor
public class AvaliacaoRepositoryImpl implements AvaliacaoRepositoryCustom {

    private final MongoTemplate mongoTemplate;


    /*
        Método responsável por calcular a média dos jogos no banco

        @param jogoId - ID do jogo a ter a média calculada
    */
    @Override
    public ResultadoAgregacaoDto calcularMediaDoJogo(String jogoId) {
        try {
            // Primeiro filtra as avaliações de um jogo pelo ID
            var matchOperation = Aggregation.match(Criteria.where("jogo.$id").is(new ObjectId(jogoId)));

            // Agrupa pelo campo 'jogo' e calcula a média e contagem
            var groupOperation = Aggregation.group("jogo.$id")
                    .avg("nota").as("mediaCalculada")
                    .count().as("totalAvaliacoes");

            // Executa a query
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);

            // Coleta os resultados
            AggregationResults<ResultadoAgregacaoDto> resultadoAgregacao = mongoTemplate
                    .aggregate(aggregation, "avaliacoes", ResultadoAgregacaoDto.class);

            return resultadoAgregacao.getUniqueMappedResult();
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao converter ID para ObjectId: " + jogoId);
            return null;
        }
    }

    @Override
    public Double calcularMediaGlobal() {
        GroupOperation groupOperation = Aggregation.group().avg("nota").as("mediaCalculada");
        Aggregation aggregation = Aggregation.newAggregation(groupOperation);

        AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "avaliacoes", Document.class);
        Document doc = result.getUniqueMappedResult();

        return doc != null ? doc.getDouble("mediaCalculada") : 0.0;
    }
}
