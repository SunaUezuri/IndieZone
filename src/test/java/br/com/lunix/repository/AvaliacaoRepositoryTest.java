package br.com.lunix.repository;

import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class AvaliacaoRepositoryTest {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private JogoRepository jogoRepository;

    private Usuario usuarioTeste;
    private Jogo jogoTeste;
    private Avaliacao avaliacao;

    @BeforeEach
    public void setup() {

        avaliacaoRepository.deleteAll();
        usuarioRepository.deleteAll();
        jogoRepository.deleteAll();

        avaliacao = new Avaliacao();

        usuarioTeste = new Usuario();
        usuarioTeste.setNome("Avaliador Padrão");
        usuarioTeste.setEmail("avaliador@teste.com");
        usuarioTeste.getRoles().add(Role.ROLE_USER);
        usuarioRepository.save(usuarioTeste);

        jogoTeste = new Jogo();
        jogoTeste.setTitulo("Jogo de Teste");
        jogoRepository.save(jogoTeste);
    }

    @Test
    public void deveSalvarAvaliacaoComReferenciasCorretas() {
        avaliacao.setUsuario(usuarioTeste);
        avaliacao.setJogo(jogoTeste);
        avaliacao.setNota(9);
        avaliacao.setComentario("Ótimo jogo!");

        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);

        assertThat(avaliacaoSalva.getId()).isNotNull();
        assertThat(avaliacaoSalva.getUsuario().getNome()).isEqualTo("Avaliador Padrão");
        assertThat(avaliacaoSalva.getJogo().getTitulo()).isEqualTo("Jogo de Teste");
        assertThat(avaliacaoSalva.getNota()).isEqualTo(9);
    }

    @Test
    public void deveEncontrarTodasAvaliacoesDeUmJogo() {
        Avaliacao avaliacao1 = new Avaliacao();
        avaliacao1.setUsuario(usuarioTeste);
        avaliacao1.setJogo(jogoTeste);
        avaliacao1.setNota(8);
        avaliacaoRepository.save(avaliacao1);

        Avaliacao avaliacao2 = new Avaliacao();
        avaliacao2.setUsuario(usuarioTeste);
        avaliacao2.setJogo(jogoTeste);
        avaliacao2.setNota(10);
        avaliacaoRepository.save(avaliacao2);

        List<Avaliacao> avaliacoesDoJogo = avaliacaoRepository.findAllByJogoOrderByDataCriacaoDesc(jogoTeste);

        assertThat(avaliacoesDoJogo).hasSize(2);
        assertThat(avaliacoesDoJogo.get(0).getNota()).isEqualTo(10);
    }

    @Test
    public void deveEncontrarTodasAvaliacoesDeUmUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setEmail("outro@avaliador.com");
        usuarioRepository.save(outroUsuario);

        Avaliacao avaliacao1 = new Avaliacao();
        avaliacao1.setUsuario(usuarioTeste);
        avaliacao1.setJogo(jogoTeste);
        avaliacao1.setNota(8);
        avaliacaoRepository.save(avaliacao1);

        Avaliacao avaliacao2 = new Avaliacao();
        avaliacao2.setUsuario(usuarioTeste);

        Jogo outroJogo = new Jogo();
        outroJogo.setTitulo("Outro Jogo");
        jogoRepository.save(outroJogo);
        avaliacao2.setJogo(outroJogo);
        avaliacao2.setNota(10);
        avaliacaoRepository.save(avaliacao2);

        Avaliacao avaliacao3 = new Avaliacao();
        avaliacao3.setUsuario(outroUsuario);
        avaliacao3.setJogo(jogoTeste);
        avaliacao3.setNota(5);
        avaliacaoRepository.save(avaliacao3);


        List<Avaliacao> avaliacoesDoUsuarioTeste = avaliacaoRepository.findAllByUsuarioOrderByDataCriacaoDesc(usuarioTeste);

        assertThat(avaliacoesDoUsuarioTeste).hasSize(2);
        assertThat(avaliacoesDoUsuarioTeste).extracting(a -> a.getJogo().getTitulo())
                .contains("Jogo de Teste", "Outro Jogo");
        assertThat(avaliacoesDoUsuarioTeste.get(0).getNota()).isEqualTo(10);
    }

    @Test
    public void deveVerificarCorretamenteSeAvaliacaoExiste() {
        avaliacao.setUsuario(usuarioTeste);
        avaliacao.setJogo(jogoTeste);
        avaliacao.setNota(5);
        avaliacaoRepository.save(avaliacao);

        Usuario outroUsuario = new Usuario();
        outroUsuario.setEmail("outro@teste.com");
        usuarioRepository.save(outroUsuario);

        boolean existe = avaliacaoRepository.existsByUsuarioAndJogo(usuarioTeste, jogoTeste);
        boolean naoExiste = avaliacaoRepository.existsByUsuarioAndJogo(outroUsuario, jogoTeste);

        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }


}
