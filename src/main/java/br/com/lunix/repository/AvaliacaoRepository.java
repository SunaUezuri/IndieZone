package br.com.lunix.repository;

import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AvaliacaoRepository extends MongoRepository<Avaliacao, String> {

    // Método para encontrar todas as avaliações de um jogo
    List<Avaliacao> findAllByJogoOrderByDataCriacaoDesc(Jogo jogo);

    // Método para encontrar todas as avaliações feitas por um usuário
    List<Avaliacao> findAllByUsuarioOrderByDataCriacaoDesc(Usuario usuario);

    // Método para verificar se uma avaliação existe com um usuário e jogo específico
    boolean existsByUsuarioAndJogo(Usuario usuario, Jogo jogo);
}
