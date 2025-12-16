package br.com.lunix.services.empresa;

import br.com.lunix.dto.empresa.EmpresaDetalhesDto;
import br.com.lunix.dto.empresa.EmpresaRequestDto;
import br.com.lunix.dto.empresa.EmpresaResponseDto;
import br.com.lunix.dto.empresa.EmpresaUpdateDto;
import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.EmpresaMapper;
import br.com.lunix.mapper.JogoMapper;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.repository.UsuarioRepository;
import br.com.lunix.services.igdb.IgdbApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository repository;
    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;

    private final IgdbApiService igdbApiService;

    private final EmpresaMapper mapper;
    private final JogoMapper jogoMapper;

    /*
        Cria uma nova empresa no sistema.
        Verifica duplicidade de nome antes de salvar.

        @param dto - Dados da nova empresa.
        return: Dados da empresa criada.
    */
    @Transactional
    public EmpresaResponseDto create(EmpresaRequestDto dto){
        if (repository.findByNomeIgnoreCase(dto.nome()).isPresent()) {
            throw new RegraDeNegocioException("Já existe uma empresa cadastrada com o nome: " + dto.nome());
        }

        Empresa entity = mapper.toEntity(dto);

        if (entity.getUrlLogo() == null || entity.getUrlLogo().isBlank()) {
            String logo = buscarLogoAutomatico(entity.getNome());
            if (logo != null) {
                entity.setUrlLogo(logo);
            }
        }

        entity = repository.save(entity);

        return mapper.toResponseDto(entity);
    }

    /*
        Lista todas as empresas registradas com paginação.

        @param page - Número da página.
        @param size - Itens por página.
    */
    @Transactional(readOnly = true)
    public Page<EmpresaResponseDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<Empresa> empresas = repository.findAll(pageable);

        return empresas.map(mapper::toResponseDto);
    }

    /*
        Busca empresas filtrando pelo país de origem.

        @param pais - Nome do país.
    */
    @Transactional(readOnly = true)
    public Page<EmpresaResponseDto> findByPais(String pais, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<Empresa> empresas = repository.findByPaisOrigemIgnoreCase(pais, pageable);

        return empresas.map(mapper::toResponseDto);
    }

    /*
        Busca empresas pelo nome das mesmas

        @param nome - Nome da empresa
    */
    @Transactional(readOnly = true)
    public Page<EmpresaResponseDto> findByNome(String nome, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<Empresa> empresas = repository.findByNomeContainingIgnoreCase(nome, pageable);

        return empresas.map(mapper::toResponseDto);
    }

    /*
        Busca os detalhes completos de uma empresa, INCLUINDO a lista de jogos dela.

        @param id - ID da empresa.
    */
    @Transactional(readOnly = true)
    public EmpresaDetalhesDto findById(String id) {
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o ID: " + id));

        // Busca os jogos vinculados a essa empresa para popular o DTO
        List<JogoResponseDto> jogosDaEmpresa = jogoRepository.findByEmpresa(empresa, Pageable.unpaged())
                .stream()
                .map(jogoMapper::toResponseDto)
                .collect(Collectors.toList());

        return mapper.toDetalhesDto(empresa, jogosDaEmpresa);
    }

    /*
        Atualiza dados de uma empresa.
        Verifica se a mudança de nome não conflita com outra empresa existente.

        @param id - ID da empresa.
        @param dto - Novos dados.
    */
    @Transactional
    public EmpresaResponseDto update(String id, EmpresaUpdateDto dto) {
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o ID: " + id));

        // Regra: Se o nome mudou, verifica se o novo nome já existe em OUTRA empresa
        if (!empresa.getNome().equalsIgnoreCase(dto.nome()) &&
                repository.findByNomeIgnoreCase(dto.nome()).isPresent()) {
            throw new RegraDeNegocioException("Já existe uma empresa cadastrada com o nome: " + dto.nome());
        }

        mapper.updateEntityFromDto(dto, empresa);

        if (empresa.getUrlLogo() == null || empresa.getUrlLogo().isBlank()) {
            String logo = buscarLogoAutomatico(empresa.getNome());
            if (logo != null) {
                empresa.setUrlLogo(logo);
            }
        }

        empresa = repository.save(empresa);

        return mapper.toResponseDto(empresa);
    }

    /*
        Deleta uma empresa do sistema.

        @param id - ID da empresa.
    */
    @Transactional
    public void delete(String id) {
        // Buscamos a empresa primeiro para garantir que existe
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

        // Verifica se existe ALGUM jogo vinculado a esta empresa
        if (jogoRepository.existsByEmpresa(empresa)) {
            throw new RegraDeNegocioException("Não é possível deletar a empresa pois existem JOGOS vinculados a ela.");
        }

        // Verifica se existe ALGUM usuário vinculado a esta empresa
        if (usuarioRepository.existsByEmpresa(empresa)) {
            throw new RegraDeNegocioException("Não é possível deletar a empresa pois existem USUÁRIOS vinculados a ela.");
        }

        // Se passou pelas validações, deleta
        repository.delete(empresa);
    }

    /*
        Método privado para validar a busca de uma logo de empresa.

        @param nomeEmpresa - nome da empresa a ser buscada
    */
    private String buscarLogoAutomatico(String nomeEmpresa) {
        return igdbApiService.buscarLogoEmpresa(nomeEmpresa);
    }
}