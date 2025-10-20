package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends MongoRepository<Empresa, String> {

    Optional<Empresa> findByNomeIgnoreCase(String nome);

    List<Empresa> findByPaisOrigemIgnoreCase(String paisOrigem);
}
