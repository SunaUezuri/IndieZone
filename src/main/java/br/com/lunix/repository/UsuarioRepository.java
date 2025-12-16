package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    /*
        Busca um usuário pelo email dele na aplicação

        @param email - Email a ser buscado no banco de dados

        return: Retorna um único usuário com email correspondente ao utilizado na busca
    */
    Optional<Usuario> findByEmail(String email);

    /*
        Busca usuários pelo nome dos mesmos.
        @param nome - Nome do usuário na aplicação.
    */
    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // Método para verificar se um usuário existe pela empresa
    boolean existsByEmpresa(Empresa empresa);
}
