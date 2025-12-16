package br.com.lunix.repository;

import br.com.lunix.model.entities.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmpresaRepository extends MongoRepository<Empresa, String> {

    // Método responsável por encontrar empresas pelo nome parcial
    Page<Empresa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // Método responsável por achar uma empresa pelo exato nome
    Optional<Empresa> findByNomeIgnoreCase(String nome);

    // Método responsável por encontrar uma empresa pelo país de origem dela
    Page<Empresa> findByPaisOrigemIgnoreCase(String paisOrigem, Pageable pageable);
}
