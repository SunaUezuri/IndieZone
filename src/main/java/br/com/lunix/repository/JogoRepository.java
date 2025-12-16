package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface JogoRepository extends MongoRepository<Jogo, String> {

    /*
        Procura jogos com o título **SEMELHANTE** ao inserido

        @param titulo - Título dos jogos a serem buscados

        return: Lista de jogos cujo o título contenha o que foi inserido
    */
    Page<Jogo> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    /*
         Procura jogos a partir do gênero inserido

         @param genero - Genero dos jogos a serem buscados(Lista de gêneros presente no Enum Genero)

         return: Lista de jogos que contém o gênero inserido
    */
    Page<Jogo> findByGeneros(Genero genero, Pageable pageable);

    /*
        Procura todos os jogos pertencentes a uma empresa específica

        @param empresa - Empresa utilizada para realizar a pesquisa

        return: Lista de jogos da empresa inserida
    */
    Page<Jogo> findByEmpresa(Empresa empresa, Pageable pageable);

    /*
        Procura jogos desenvolvidos por um único DEV (ex: Toby fox)

        @param devAutonomo - Nome de usuário do dev do jogo

        return: Lista de jogos feitos por aquele DEV
    */
    Page<Jogo> findByDevAutonomo(Usuario devAutonomo, Pageable pageable);

    /*
        Busca os 10 jogos mais bem avaliados da aplicação

        return: Retorna uma lista com os 10 jogos com as maiores notas médias da aplicação
    */
    List<Jogo> findTop10ByOrderByNotaMediaDesc();

    /*
        Busca os 10 jogos recentemente inseridos

        return: Retorna uma lista com os 10 jogos que foram lançados
        recentemente.
    */
    List<Jogo> findTop10ByOrderByDataLancamentoDesc();

    /*
      Busca todos os jogos que contêm uma plataforma específica em sua lista.
      Essencial para a funcionalidade de filtro por plataforma.
    */
    Page<Jogo> findByPlataformas(Plataforma plataforma, Pageable pageable);

    // Encontra jogos que falharam na sincronização (lista de preços vazia)
    @Query("{ 'precos': { $exists: true, $size: 0 } }")
    Page<Jogo> findByPrecosIsEmpty(Pageable pageable);

    // Conta os jogos por genero
    long countByGeneros(Genero genero);

    // Conta os jogos pela plataforma
    long countByPlataformas(Plataforma plataforma);

    // Método para verificar se existe jogos com essa empresa
    boolean existsByEmpresa(Empresa empresa);
}
