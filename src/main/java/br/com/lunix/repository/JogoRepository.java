package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface JogoRepository extends MongoRepository<Jogo, String> {

    List<Jogo> findByTituloContainingIgnoreCase(String titulo);

    List<Jogo> findByGenero(Genero genero);

    List<Jogo> findByEmpresa(Empresa empresa);

    List<Jogo> findByClassificacao(ClassificacaoIndicativa classificacao);

    List<Jogo> findTop10OrderByNotaMediaDesc();

    List<Jogo> findTop10ByOrderByDataLancamentoDesc();
}
