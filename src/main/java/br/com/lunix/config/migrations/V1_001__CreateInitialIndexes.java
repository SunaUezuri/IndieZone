package br.com.lunix.config.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@ChangeUnit(id = "create-initial-indexes", order = "001", author = "lunix-dev")
public class V1_001__CreateInitialIndexes {

    @Execution
    public void createUserEmailIndex(MongoTemplate template) {
        System.out.println("MONGOCK[001]: Criando indíce único para o email de usuário...");
        template.indexOps("usuarios").createIndex(new Index().on("email", Sort.Direction.ASC).unique());
    }

    @RollbackExecution
    public void rollback(MongoTemplate template) {
        template.indexOps("usuarios").dropIndex("email_1");
    }
}
