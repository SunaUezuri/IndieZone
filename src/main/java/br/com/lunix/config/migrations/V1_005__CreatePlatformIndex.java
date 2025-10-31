package br.com.lunix.config.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/*
    Classe de migração responsável por criar um
    indice para o campo de plataformas criando
    pesquisas mais dinâmicas.
*/
@ChangeUnit(id = "create-platform-index", order = "005", author = "lunix-dev")
public class V1_005__CreatePlatformIndex {

    /*
        Método de execução que cria o indice
        no campo plataformas da collection de
        jogos.
    */
    @Execution
    public void createPlatformIndex(MongoTemplate template) {
        System.out.println("MONGOCK[005]: Criando um índice multichave em jogos.plataformas");
        template.indexOps("jogos")
                .createIndex(new Index().on("plataformas", Sort.Direction.ASC));
    }

    /*
        ROllback em caso de problemas deletando
        o indice criado.
    */
    @RollbackExecution
    public void rollback(MongoTemplate template) {
        template.indexOps("jogos").dropIndex("plataformas_1");
    }
}
