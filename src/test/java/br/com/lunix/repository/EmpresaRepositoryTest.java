package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

@DataMongoTest
@TestPropertySource(properties = "mongock.enabled=false")
public class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Empresa empresa;

    @BeforeEach
    public void setup() {
        repository.deleteAll();
        empresa = new Empresa();
    }

    @Test
    public void deveSalvarEBuscarEmpresaPorIdComSucesso() {
        empresa.setNome("Team Cherry");
        empresa.setPaisOrigem("Austrália");
        empresa.setDescricao("Desenvolvedores de Hollow Knight");
        Empresa empresaSalva = repository.save(empresa);
        Optional<Empresa> empresaOptional = repository.findById(empresaSalva.getId());

        assertThat(empresaOptional).isPresent();
        assertThat(empresaOptional.get().getId()).isNotNull();
        assertThat(empresaOptional.get().getNome()).isEqualTo("Team Cherry");
        assertThat(empresaOptional.get().getPaisOrigem()).isEqualTo("Austrália");
        assertThat(empresaOptional.get().getDescricao()).isEqualTo("Desenvolvedores de Hollow Knight");
    }

    @Test
    public void deveEncontrarEmpresaPeloNomeIgnorandoCase() {
        empresa.setNome("Team Cherry");
        empresa.setPaisOrigem("Austrália");
        empresa.setDescricao("Desenvolvedores de Hollow Knight");
        repository.save(empresa);

        Optional<Empresa> buscadaPorMinusculas = repository.findByNomeIgnoreCase("team cherry");
        Optional<Empresa> buscadaPorMaiusculas = repository.findByNomeIgnoreCase("TEAM CHERRY");

        assertThat(buscadaPorMinusculas).isPresent();
        assertThat(buscadaPorMinusculas.get().getNome()).isEqualTo("Team Cherry");
        assertThat(buscadaPorMaiusculas).isPresent();
        assertThat(buscadaPorMaiusculas.get().getNome()).isEqualTo("Team Cherry");
    }

    @Test
    public void deveBuscarEmpresasPorPaisIgnorandoCase() {
        Empresa empresa1 = new Empresa();
        empresa1.setNome("Empresa BR1");
        empresa1.setPaisOrigem("BRASIL");
        repository.save(empresa1);

        Empresa empresa2 = new Empresa();
        empresa2.setNome("Empresa BR2");
        empresa2.setPaisOrigem("brasil");
        repository.save(empresa2);

        Empresa empresa3 = new Empresa();
        empresa3.setNome("Empresa US");
        empresa3.setPaisOrigem("EUA");
        repository.save(empresa3);

        List<Empresa> empresasBrasileiras = repository.findByPaisOrigemIgnoreCase("brasil");

        assertThat(empresasBrasileiras).hasSize(2);
        assertThat(empresasBrasileiras).extracting(Empresa::getNome)
                .containsExactlyInAnyOrder("Empresa BR1", "Empresa BR2");
    }

    @Test
    public void naoDeveEncontrarEmpresaComNomeInexistente() {
        empresa.setNome("Inexistente");
        repository.save(empresa);

        Optional<Empresa> empresaNaoExiste = repository.findByNomeIgnoreCase("Ubisoft");

        assertThat(empresaNaoExiste).isNotPresent();
    }
}
