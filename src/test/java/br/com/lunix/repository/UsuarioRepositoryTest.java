package br.com.lunix.repository;

import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private MongoTemplate template;

    @BeforeEach
    void celeanup() {
        repository.deleteAll();
    }

    @Test
    public void deveSalvarEBuscarUsuarioPorEmailComSucesso() {
        // Arrange
        Usuario usuario = new Usuario();

        usuario.setNome("WesleyDev");
        usuario.setEmail("wesley@IndieZone.com");
        usuario.setSenha("SenhaCriptografada");
        usuario.getRoles().add(Role.ROLE_DEV);

        // Act
        repository.save(usuario);
        Optional<Usuario> usuarioOptional = repository.findByEmail("wesley@IndieZone.com");

        // Assert
        assertThat(usuarioOptional).isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isNotNull();
                    assertThat(u.getNome()).isEqualTo("WesleyDev");
                    assertThat(u.getRoles()).containsExactly(Role.ROLE_DEV);
                });
    }
}
