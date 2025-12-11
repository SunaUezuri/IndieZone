package br.com.lunix.services;

import br.com.lunix.dto.empresa.EmpresaDetalhesDto;
import br.com.lunix.dto.empresa.EmpresaRequestDto;
import br.com.lunix.dto.empresa.EmpresaResponseDto;
import br.com.lunix.dto.empresa.EmpresaUpdateDto;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.EmpresaMapper;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository repository;
    private final EmpresaMapper mapper;

    /*
        Método responsável por criar uma nova empresa no sistema.
        @param dto Objeto de requisição do projeto.
    */
    @Transactional
    public EmpresaResponseDto create(EmpresaRequestDto dto){
        if (repository.findByNomeIgnoreCase(dto.nome()).isPresent()) {
            throw new RegraDeNegocioException("Já existe uma empresa cadastrada com o nome: " + dto.nome());
        }

        Empresa entity = mapper.toEntity(dto);
        entity = repository.save(entity);

        return mapper.toResponseDto(entity);
    }

    /*
        Lista todas as empresas registradas com paginação.
        @param page Número da página.
        @param size Quantidade de itens por página.
    */
    @Transactional(readOnly = true)
    public Page<EmpresaResponseDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<Empresa> empresas = repository.findAll(pageable);

        return empresas.map(mapper::toResponseDto);
    }

    /*
        Busca empresas pelo país com paginação.
        @param page Número da página.
        @param size Quantidade de itens por página.
        @param pais Nome do país para realizar a pesquisa.
    */
    @Transactional(readOnly = true)
    public Page<EmpresaResponseDto> findByPais(String pais, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<Empresa> empresas = repository.findByPaisOrigemIgnoreCase(pais, pageable);

        return empresas.map(mapper::toResponseDto);
    }

    /*
        Busca os detalhes de uma empresa pelo ID.
        @param id ID do objeto a ser visualizado.
    */
    @Transactional(readOnly = true)
    public EmpresaDetalhesDto findById(String id) {
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o ID: " + id));

        return mapper.toDetalhesDto(empresa, Collections.emptyList());
    }

    /*
        Atualiza uma empresa existente no sistema.
        @param id ID do objeto a se atualizar.
        @param dto Corpo do objeto a ser atualizado.
    */
    public EmpresaResponseDto update(String id, EmpresaUpdateDto dto) {
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o ID: " + id));

        // Regra de negócio da aplicação, caso o nome mude verifica se o novo nome já não existe
        if (!empresa.getNome().equalsIgnoreCase(dto.nome())) {
            if (repository.findByNomeIgnoreCase(dto.nome()).isPresent()) {
                throw new RegraDeNegocioException("Já existe uma empresa cadastrada com o nome: " + dto.nome());
            }
        }

        mapper.updateEntityFromDto(dto, empresa);
        empresa = repository.save(empresa);

        return mapper.toResponseDto(empresa);
    }

    /*
        Deleta uma empresa registrada no banco de dados.
        @param id ID da empresa a ser deletada.
    */
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada com ID: " + id);
        }

        repository.deleteById(id);
    }
}
