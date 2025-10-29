package br.com.lunix.config.migrations;

import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

// Classe de migração para a inserção inicial de usuários e empresas
@ChangeUnit(id = "seed-users-and-companies", order = "002", author = "lunix-dev")
public class V1_002__SeedUsersAndCompanies {

    public static Usuario admin, devAutonomo, user;
    public static Empresa empresa;

    /*
        Método de execução de migração que insere usuários
        e empresas para dados iniciais da aplicação.

        @param template - template do mongo para configurações como
        deletar coleções e inserir os dados.

        @param encoder - encoder para decodificar a senha.
    */
    @Execution
    public void seedInitialUsersAndCompanies(MongoTemplate template, PasswordEncoder encoder) {
        /*
            O método primeiramente deleta as coleções para deletar
            os dados utilizados nos testes de integração e depois
            cria 1 usuário para cada tipo de perfil e os insere no banco.
        */
        System.out.println("MONGOCK[002]: Inserindo usuários e empresas iniciais...");
        template.dropCollection("usuarios");
        template.dropCollection("empresas");

        admin = new Usuario();
        admin.setNome("Admin Lunix");
        admin.setEmail("admin@lunix.com");
        admin.setSenha(encoder.encode("admin123"));
        admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        template.save(admin);

        devAutonomo = new Usuario();
        devAutonomo.setNome("Toby Fox");
        devAutonomo.setEmail("toby@fox.com");
        devAutonomo.setSenha(encoder.encode("dev123"));
        devAutonomo.setRoles(Set.of(Role.ROLE_DEV));
        template.save(devAutonomo);

        user = new Usuario();
        user.setNome("User");
        user.setEmail("user@gmail.com");
        user.setSenha(encoder.encode("user123"));
        user.setRoles(Set.of(Role.ROLE_USER));
        template.save(user);

        empresa = new Empresa();
        empresa.setNome("Team Cherry");
        empresa.setPaisOrigem("Austrália");
        empresa.setDescricao("Team Cherry é uma pequena, independente desenvolvedora de jogos situada em Adelaide, Austrália do Sul, fundada em 2014.");
        template.save(empresa);
    }

    /*
        Método de Rollback em caso de erro que deleta as
        coleções.
    */
    @RollbackExecution
    public void rollback(MongoTemplate template) {
        template.dropCollection("empresas");
        template.dropCollection("usuarios");
    }
}
