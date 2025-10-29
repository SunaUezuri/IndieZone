package br.com.lunix.config.migrations;

import com.mongodb.client.model.Indexes;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/*
    Classe de migração para criar indices de query
    para maior perfomance de pesquisas.
*/
@ChangeUnit(id = "create-query-indexes", order = "004", author = "lunix-dev")
public class V1_004__CreateQueryIndexes {

    /*
        Método de execução que pega as coleções alvo
        e gera indices para cada parâmetro preciso.
    */
    @Execution
    public void createPerfomanceIndexes(MongoTemplate template) {
        System.out.println("MONGOCK[004]: Criando índices de perfomance...");

        template.getCollection("jogos").createIndex(Indexes.text("titulo"));
        template.indexOps("jogos").createIndex(new Index().on("generos", Sort.Direction.ASC));

        template.indexOps("avaliacoes").createIndex(new Index().on("jogo", Sort.Direction.ASC));
        template.indexOps("avaliacoes").createIndex(new Index().on("usuario", Sort.Direction.ASC));

        template.indexOps("empresas").createIndex(new Index().on("nome", Sort.Direction.ASC));
    }

    /*
        Método de rollback para em caso de erro na
        execução sejam deletados os indices.
    */
    @RollbackExecution
    public void rollback(MongoTemplate template) {
        template.indexOps("jogos").dropIndex("titulo_text");
        template.indexOps("avaliacoes").dropIndex("jogo_1");
        template.indexOps("avaliacoes").dropIndex("usuario_1");
        template.indexOps("jogos").dropIndex("generos_1");
        template.indexOps("empresas").dropIndex("nome_1");
    }
}
