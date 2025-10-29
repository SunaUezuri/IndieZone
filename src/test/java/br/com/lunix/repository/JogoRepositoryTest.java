package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.PrecoPlataforma;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/*
    Classe de teste para garantir o funcionamento
    dos métodos do repositório de Jogo
*/
@DataMongoTest
@TestPropertySource(properties = "mongock.enabled=false")
public class JogoRepositoryTest {

    @Autowired
    private JogoRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Empresa devTest;

    private Jogo jogo;

    @BeforeEach
    public void setup() {
        repository.deleteAll();
        empresaRepository.deleteAll();

        jogo = new Jogo();

        devTest = new Empresa();
        devTest.setNome("Estúdio Teste");
        devTest.setPaisOrigem("Brasil");
        empresaRepository.save(devTest);
    }

    /*
        Garante que um jogo seja salvo com seus relacionamentos
        e objetos embutidos
    */
    @Test
    public void deveSalvarEBuscarJogoComRelacionamentosEObjetosEmbutidos() {
        Jogo novoJogo = new Jogo();
        novoJogo.setTitulo("Aventura Completa");
        novoJogo.setEmpresa(devTest);
        novoJogo.setGeneros(List.of(Genero.AVENTURA, Genero.ACAO));

        PrecoPlataforma precoSteam = new PrecoPlataforma("Steam", 49.90, 99.90, 50, "url/steam");
        PrecoPlataforma precoGog = new PrecoPlataforma("GOG", 99.90, 99.90, 0, "url/gog");
        novoJogo.setPrecos(List.of(precoSteam, precoGog));

        Jogo jogoSalvo = repository.save(novoJogo);
        Optional<Jogo> jogoBuscado = repository.findById(jogoSalvo.getId());

        assertThat(jogoBuscado).isPresent();
        Jogo jogoEncontrado = jogoBuscado.get();

        assertThat(jogoEncontrado.getTitulo()).isEqualTo("Aventura Completa");
        assertThat(jogoEncontrado.getEmpresa().getNome()).isEqualTo("Estúdio Teste");
        assertThat(jogoEncontrado.getGeneros()).contains(Genero.AVENTURA, Genero.ACAO);

        assertThat(jogoEncontrado.getPrecos()).isNotNull();
        assertThat(jogoEncontrado.getPrecos()).hasSize(2);
        assertThat(jogoEncontrado.getPrecos().get(0).getNomeLoja()).isEqualTo("Steam");
        assertThat(jogoEncontrado.getPrecos().get(0).getPrecoAtual()).isEqualTo(49.90);
    }

    /*
        Teste para garantir que um jogo seja encontrado
        comum título semelhante sem incluir case.
    */
    @Test
    public void deveEncontrarJogoPeloTituloParcialIgnorandoCase() {
        jogo.setTitulo("Meu Jogo Fantástico");
        repository.save(jogo);

        List<Jogo> resultado = repository.findByTituloContainingIgnoreCase("jogo fantás");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Meu Jogo Fantástico");

    }

    /*
        Teste para garantir que são encontrados jogos
        a partir dos gêneros inseridos.
    */
    @Test
    public void deveEncontrarJogosPorGenero() {
        Jogo jogoIndieAventura = new Jogo();
        jogoIndieAventura.setTitulo("Aventura Indie");
        jogoIndieAventura.setGeneros(List.of(Genero.ACAO, Genero.AVENTURA));
        repository.save(jogoIndieAventura);

        Jogo jogoIndieRpg = new Jogo();
        jogoIndieRpg.setTitulo("RPG Indie");
        jogoIndieRpg.setGeneros(List.of(Genero.SOULS_LIKE, Genero.RPG));
        repository.save(jogoIndieRpg);

        Jogo jogoAcao = new Jogo();
        jogoAcao.setTitulo("Ação Frenética");
        jogoAcao.setGeneros(List.of(Genero.ACAO));
        repository.save(jogoAcao);

        List<Jogo> jogosAcao = repository.findByGeneros(Genero.ACAO);
        List<Jogo> jogosAventura = repository.findByGeneros(Genero.AVENTURA);

        assertThat(jogosAcao).hasSize(2);
        assertThat(jogosAcao).extracting(Jogo::getTitulo).contains("Aventura Indie", "Ação Frenética");
        assertThat(jogosAventura).hasSize(1);
        assertThat(jogosAventura.get(0).getTitulo()).isEqualTo("Aventura Indie");
    }

    /*
        Teste para garantir que encontre todos os jogos
        com a classificação indicativa inserida.
    */
    @Test
    public void deveEncontrarJogosPorClassificacaoIndicativa() {
        Jogo jogoLivre1 = new Jogo();
        jogoLivre1.setTitulo("Fazendinha Feliz");
        jogoLivre1.setClassificacao(ClassificacaoIndicativa.LIVRE);
        repository.save(jogoLivre1);

        Jogo jogoLivre2 = new Jogo();
        jogoLivre2.setTitulo("Corrida de Kart");
        jogoLivre2.setClassificacao(ClassificacaoIndicativa.LIVRE);
        repository.save(jogoLivre2);

        Jogo jogoAdulto = new Jogo();
        jogoAdulto.setTitulo("Guerra Sombria");
        jogoAdulto.setClassificacao(ClassificacaoIndicativa.DEZOITO);
        repository.save(jogoAdulto);

        List<Jogo> jogosLivres = repository.findByClassificacao(ClassificacaoIndicativa.LIVRE);

        assertThat(jogosLivres).hasSize(2);
        assertThat(jogosLivres).extracting(Jogo::getTitulo).contains("Fazendinha Feliz", "Corrida de Kart");
    }

    /*
        Teste para garantir que o método encontre
        todos os jogos de uma empresa específica.
    */
    @Test
    public void deveEncontrarJogosPorEmpresa() {
        Jogo jogo1 = new Jogo();
        jogo1.setTitulo("Jogo 1");
        jogo1.setEmpresa(devTest);
        repository.save(jogo1);

        Jogo jogo2 = new Jogo();
        jogo2.setTitulo("Jogo 2");
        jogo2.setEmpresa(devTest);
        repository.save(jogo2);

        List<Jogo> resultado = repository.findByEmpresa(devTest);
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Jogo::getTitulo).contains("Jogo 1", "Jogo 2");
    }

    /*
        Teste para garantir que o método encontre
        jogos de um dev específico.
    */
    @Test
    public void deveEncontrarJogosPorDev() {
        Usuario usuario = new Usuario();
        usuario.setNome("Wesley");
        usuario.setEmail("w@gmail.com");
        usuarioRepository.save(usuario);

        Jogo jogo1 = new Jogo();
        jogo1.setTitulo("Jogo 1");
        jogo1.setDevAutonomo(usuario);
        repository.save(jogo1);

        Jogo jogo2 = new Jogo();
        jogo2.setTitulo("Jogo 2");
        jogo2.setDevAutonomo(usuario);
        repository.save(jogo2);

        List<Jogo> resultado = repository.findByDevAutonomo(usuario);
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Jogo::getTitulo).contains("Jogo 1", "Jogo 2");
    }

    /*
        Teste para garantir que encontre os 10 jogos
        com maior nota média do banco.
    */
    @Test
    public void deveRetornarTop10JogosOrdenadosPorNotaMedia() {
        for (int i = 0; i <= 10; i++) {
            Jogo jogo = new Jogo();
            jogo.setTitulo("Jogo Nota " + i);
            jogo.setNotaMedia(i);
            repository.save(jogo);
        }

        List<Jogo> top10 = repository.findTop10ByOrderByNotaMediaDesc();
        assertThat(top10).hasSize(10);
        assertThat(top10.get(0).getTitulo()).isEqualTo("Jogo Nota 10");
        assertThat(top10.get(9).getTitulo()).isEqualTo("Jogo Nota 1");
    }

    /*
        Teste para garantir que sejam encontrados
        os 10 jogos recentemente lançados.
    */
    @Test
    public void deveRetornarTop10JogosOrdenadosPorDataLancamento() {
        for (int i = 1; i <= 11; i++) {
            Jogo jogo = new Jogo();
            jogo.setTitulo("Jogo Dia " + i);
            jogo.setDataLancamento(LocalDate.of(2025, 1, i));
            repository.save(jogo);
        }

        List<Jogo> recentes = repository.findTop10ByOrderByDataLancamentoDesc();
        assertThat(recentes).hasSize(10);
        assertThat(recentes.get(0).getTitulo()).isEqualTo("Jogo Dia 11");
        assertThat(recentes.get(9).getTitulo()).isEqualTo("Jogo Dia 2");
    }

}
