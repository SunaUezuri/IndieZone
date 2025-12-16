package br.com.lunix.services.jogo;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.jogos.*;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.AvaliacaoMapper;
import br.com.lunix.mapper.JogoMapper;
import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.AvaliacaoRepository;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/*
    Service responsável por orquestração das atividades de jogos
    da aplicação
*/
@Service
@RequiredArgsConstructor
public class JogoService {

    private final JogoRepository jogoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    private final JogoMapper jogoMapper;
    private final AvaliacaoMapper avaliacaoMapper;

    private final JogoSecurityService securityService;
    private final JogoPrecoService precoService;

    /*
        Método responsável por cadastrar um novo jogo na plataforma.

        @param requestAdmin - DTO contendo os dados do jogo e possíveis vínculos de empresa/dev.
        return: Retorna o DTO do jogo recém-criado.
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-top10-nota", allEntries = true),
            @CacheEvict(value = "jogos-top10-recentes", allEntries = true)
    })
    public JogoResponseDto cadastrar(JogoAdminRequestDto requestAdmin) {
        Jogo jogo = jogoMapper.toEntity(requestAdmin.jogoData());

        // Delega a lógica de permissão/dono
        securityService.definirDonoDoJogo(jogo, requestAdmin);

        jogo = jogoRepository.save(jogo);

        // Delega a integração de preço
        precoService.enviarParaFila(jogo.getId());

        return jogoMapper.toResponseDto(jogo);
    }

    /*
        Atualiza dados cadastrais de um jogo.
        Valida se o usuário tem permissão sobre aquele jogo específico.
        Limpa caches relacionados para garantir consistência dos dados.

        @param id - ID do jogo.
        @param dto - Novos dados.
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-detalhes", key = "#id"),
            @CacheEvict(value = "jogos-top10-nota", allEntries = true),
            @CacheEvict(value = "jogos-top10-recentes", allEntries = true)
    })
    public JogoResponseDto atualizar(String id, JogoUpdateDto dto) {
        Jogo jogo = buscarPorId(id);

        // Validação delegada
        securityService.validarPermissaoEdicao(jogo);

        jogoMapper.updateEntityFromDto(dto, jogo);
        jogo = jogoRepository.save(jogo);

        return jogoMapper.toResponseDto(jogo);
    }

    /*
        Atualiza apenas a lista de gêneros de um jogo (PATCH).

        @param id - ID do jogo.
        @param dto - Lista nova de gêneros.
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-detalhes", key = "#id"),
            @CacheEvict(value = "jogos-top10-nota", allEntries = true),
            @CacheEvict(value = "jogos-top10-recentes", allEntries = true)
    })
    public void patchGeneros(String id, JogoGenresPatchDto dto) {
        Jogo jogo = buscarPorId(id);
        securityService.validarPermissaoEdicao(jogo);

        jogoMapper.updateGeneros(dto, jogo);
        jogoRepository.save(jogo);
    }


    /*
        Deleta um jogo do sistema.
        Valida permissões e limpa todos os caches afetados.

        @param id - ID do jogo a ser deletado.
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-detalhes", key = "#id"),
            @CacheEvict(value = "jogos-top10-nota", allEntries = true),
            @CacheEvict(value = "jogos-top10-recentes", allEntries = true)
    })
    public void deletar(String id) {
        Jogo jogo = buscarPorId(id);
        securityService.validarPermissaoEdicao(jogo);
        jogoRepository.delete(jogo);
    }

    // --- Consultas (Read Only) ---

    @Transactional(readOnly = true)
    @Cacheable(value = "jogos-detalhes", key = "#id")
    public JogoDetalhesDto buscarDetalhesPorId(String id) {
        Jogo jogo = buscarPorId(id);
        List<Avaliacao> todasAvaliacoes = avaliacaoRepository.findAllByJogoOrderByDataCriacaoDesc(jogo);

        return jogoMapper.toDetalhesDto(jogo,
                mapReviewsByRole(todasAvaliacoes, Role.ROLE_ADMIN),
                mapReviewsByRole(todasAvaliacoes, Role.ROLE_DEV),
                mapReviewsByRole(todasAvaliacoes, Role.ROLE_USER));
    }

    @Transactional(readOnly = true)
    public Page<JogoResponseDto> listarTodos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findAll(pageable).map(jogoMapper::toResponseDto);
    }

    // ... buscarPorTitulo, buscarPorGenero, buscarPorPlataforma (Mantém iguais) ...

    /*
        Realiza a busca de jogos pelo título (Search Bar).
        A busca é parcial (containing) e ignora maiúsculas/minúsculas.

        @param titulo - Termo a ser pesquisado.
        @param page - Número da página.
        @param size - Quantidade de itens por página.
        return: Página com os jogos que correspondem ao termo pesquisado.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorTitulo(String titulo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("titulo").ascending());
        return jogoRepository.findByTituloContainingIgnoreCase(titulo, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Filtra os jogos por um Gênero específico (ex: RPG, ACAO).

        @param genero - Enum do gênero desejado.
        @param page - Número da página.
        @param size - Quantidade de itens por página.
        return: Página com os jogos do gênero selecionado.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorGenero(Genero genero, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByGeneros(genero, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Filtra os jogos por Plataforma (ex: PC, PS5, SWITCH).

        @param plataforma - Enum da plataforma desejada.
        @param page - Número da página.
        @param size - Quantidade de itens por página.
        return: Página com os jogos disponíveis na plataforma.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorPlataforma(Plataforma plataforma, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByPlataformas(plataforma, pageable).map(jogoMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorEmpresa(String empresaId, int page, int size) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByEmpresa(empresa, pageable).map(jogoMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorDev(String devId, int page, int size) {
        Usuario dev = usuarioRepository.findById(devId)
                .orElseThrow(() -> new ResourceNotFoundException("Dev não encontrado."));
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByDevAutonomo(dev, pageable).map(jogoMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<JogoResponseDto> listarMeusJogos(int page, int size) {
        Usuario usuario = securityService.getUsuarioLogado();
        Pageable pageable = PageRequest.of(page, size, Sort.by("titulo").ascending());

        if (usuario.getRoles().contains(Role.ROLE_ADMIN)) {
            return jogoRepository.findAll(pageable).map(jogoMapper::toResponseDto);
        }
        if (usuario.getEmpresa() != null) {
            return jogoRepository.findByEmpresa(usuario.getEmpresa(), pageable).map(jogoMapper::toResponseDto);
        } else {
            return jogoRepository.findByDevAutonomo(usuario, pageable).map(jogoMapper::toResponseDto);
        }
    }

    /*
        Busca os 10 jogos com melhor nota média.
        Resultado é armazenado em cache para performance da Home Page.
    */
    @Cacheable(value = "jogos-top10-nota")
    public List<JogoResponseDto> buscarTop10MelhoresAvaliados() {
        return jogoRepository.findTop10ByOrderByNotaMediaDesc().stream().map(jogoMapper::toResponseDto).toList();
    }

    /*
        Busca os 10 jogos lançados mais recentemente.
        Resultado é armazenado em cache.
    */
    @Cacheable(value = "jogos-top10-recentes")
    public List<JogoResponseDto> buscarTop10Lancamentos() {
        return jogoRepository.findTop10ByOrderByDataLancamentoDesc().stream().map(jogoMapper::toResponseDto).toList();
    }


    private Jogo buscarPorId(String id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado. ID: " + id));
    }

    private List<AvaliacaoResponseDto> mapReviewsByRole(List<Avaliacao> avaliacoes, Role role) {
        return avaliacoes.stream()
                .filter(av -> av.getUsuario().getRoles().contains(role))
                .map(avaliacaoMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}