package br.com.lunix.config.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/*
    Classe de migração para criar Indices para deixar a pesquisa
    mais perfomática
*/
@ChangeUnit(id = "create-initial-indexes", order = "001", author = "lunix-dev")
public class V1_001__CreateInitialIndexes {

    /*
        Método que cria Indice com valor único para o campo de email
        no documento

        @param template - Template para acesso a métodos do mongo para configurações como
        criação de indices e de documentos
    */
    @Execution
    public void createUserEmailIndex(MongoTemplate template) {
        System.out.println("MONGOCK[001]: Criando indíce único para o email de usuário...");
        template.indexOps("usuarios").createIndex(new Index().on("email", Sort.Direction.ASC).unique());
    }

    /*
        Método de rollback para retirar os indíces em
        caso de algum problema
    */
    @RollbackExecution
    public void rollback(MongoTemplate template) {
        template.indexOps("usuarios").dropIndex("email_1");
    }
}
