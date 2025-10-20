package br.com.lunix.repository;

import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AvaliacaoRepository extends MongoRepository<Avaliacao, String> {

    List<Avaliacao> findAllByJogoOrderByDataCriacaoDesc(Jogo jogo);

    List<Avaliacao> findAllByUsuarioOrderByDataCriacaoDesc(Usuario usuario);

    boolean existsByUsuarioAndJogo(Usuario usuario, Jogo jogo);
}
