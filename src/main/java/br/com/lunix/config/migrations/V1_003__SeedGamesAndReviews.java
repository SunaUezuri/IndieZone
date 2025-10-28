package br.com.lunix.config.migrations;

import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.util.List;

import static br.com.lunix.config.migrations.V1_002__SeedUsersAndCompanies.*;

@ChangeUnit(id = "seed-games-and-reviews", order = "003", author = "lunix-dev")
public class V1_003__SeedGamesAndReviews {

    @Execution
    public void seedInitialGamesAndReviews(MongoTemplate template) {
        System.out.println("MONGOCK[003]: Inserindo jogos e avaliações iniciais...");

        Jogo undertale = new Jogo();
        undertale.setTitulo("Undertale");
        undertale.setDescricao("Um RPG onde ninguém precisa morrer.");
        undertale.setDevAutonomo(devAutonomo);
        undertale.setDataLancamento(LocalDate.of(2015, 9, 15));
        undertale.setClassificacao(ClassificacaoIndicativa.DOZE);
        undertale.setGeneros(List.of(Genero.RPG, Genero.AVENTURA));
        template.save(undertale);

        Jogo hollowKnight = new Jogo();
        hollowKnight.setTitulo("Hollow Knight");
        hollowKnight.setDescricao("Explore um vasto reino de insetos e descubra como parar a infecção");
        hollowKnight.setEmpresa(empresa);
        hollowKnight.setDataLancamento(LocalDate.of(2017, 2, 24));
        hollowKnight.setClassificacao(ClassificacaoIndicativa.DEZ);
        hollowKnight.setGeneros(List.of(Genero.METROIDVANIA, Genero.AVENTURA));
        template.save(hollowKnight);

        Avaliacao avaliacaoAdmin = new Avaliacao();
        avaliacaoAdmin.setUsuario(admin);
        avaliacaoAdmin.setJogo(hollowKnight);
        avaliacaoAdmin.setNota(10);
        avaliacaoAdmin.setUsuario(admin);
        avaliacaoAdmin.setComentario("Análise do Admin: Uma obra-prima do gênero metroidvania.");
        template.save(avaliacaoAdmin);

        Avaliacao avaliacaoUser = new Avaliacao();
        avaliacaoUser.setUsuario(user);
        avaliacaoUser.setJogo(hollowKnight);
        avaliacaoUser.setNota(9);
        avaliacaoUser.setUsuario(user);
        avaliacaoUser.setComentario("Muito desafiador, mas recompensador. Adorei!");
        template.save(avaliacaoUser);
    }

    @RollbackExecution
    public void rollback(MongoTemplate template) {
        template.dropCollection("avaliacoes");
        template.dropCollection("jogos");
    }
}
