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

@ChangeUnit(id = "seed-users-and-companies", order = "002", author = "lunix-dev")
public class V1_002__SeedUsersAndCompanies {

    public static Usuario admin, devAutonomo, user;
    public static Empresa empresa;

    @Execution
    public void seedInitialUsersAndCompanies(MongoTemplate template, PasswordEncoder encoder) {
        System.out.println("MONGOCK[002]: Inserindo usuários e empresas iniciais...");

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

    @RollbackExecution
    public void rollback(MongoTemplate template) {
        template.dropCollection("empresas");
        template.dropCollection("usuarios");
    }
}
