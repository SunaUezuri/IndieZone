package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends MongoRepository<Empresa, String> {

    // Método responsável por encontrar uma empresa pelo nome da mesma
    Optional<Empresa> findByNomeIgnoreCase(String nome);

    // Método responsável por encontrar uma empresa pelo país de origem dela
    List<Empresa> findByPaisOrigemIgnoreCase(String paisOrigem);
}
