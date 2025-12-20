package br.com.lunix.services.jogo;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.jogos.*;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.AvaliacaoMapper;
import br.com.lunix.mapper.JogoMapper;
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

    /*
        Busca um jogo para mais detalhes sobre o mesmo

        @param id - Identificador único do jogo
    */
    @Transactional(readOnly = true)
    @Cacheable(value = "jogos-detalhes", key = "#id")
    public JogoDetalhesDto buscarDetalhesPorId(String id) {
        Jogo jogo = buscarPorId(id);

        Pageable top3 = PageRequest.of(0,3, Sort.by("dataCriacao").descending());

        List<Usuario> admins = usuarioRepository.findByRolesContains(Role.ROLE_ADMIN);
        List<Usuario> devs = usuarioRepository.findByRolesContains(Role.ROLE_DEV);
        List<Usuario> users = usuarioRepository.findApenasUsuariosComuns();

        var reviewsAdmin = buscarReviewsFiltradas(jogo, admins, top3);
        var reviewsDev = buscarReviewsFiltradas(jogo, devs, top3);
        var reviewsUser = buscarReviewsFiltradas(jogo, users, top3);

        return jogoMapper.toDetalhesDto(jogo, reviewsAdmin, reviewsDev, reviewsUser);
    }

    /*
        Método que lista todos os jogos da aplicação.

        @param page - Número da página.
        @param size - Quantidade de itens por página.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> listarTodos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findAll(pageable).map(jogoMapper::toResponseDto);
    }

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
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorPlataforma(Plataforma plataforma, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByPlataformas(plataforma, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Filtra jogos feitos por uma empresa

        @param empresaId - Identificador único da empresa.
        @param page - Número da página.
        @param size - Quantidade de itens por página.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorEmpresa(String empresaId, int page, int size) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByEmpresa(empresa, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Método para buscar jogos feitos pro um dev autônomo

        @param devId - Identificador único do desenvolvedor
        @param page - Número da página a consultar
        @param size - quantidade de itens que devem aparecer
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorDev(String devId, int page, int size) {
        Usuario dev = usuarioRepository.findById(devId)
                .orElseThrow(() -> new ResourceNotFoundException("Dev não encontrado."));
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByDevAutonomo(dev, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Método responsável por listar os jogos
        feitos por um usuário.

        @param page - Número da página a consultar
        @param size - quantidade de itens que devem aparecer
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> listarMeusJogos(int page, int size) {
        Usuario usuario = securityService.getUsuarioLogado();
        Pageable pageable = PageRequest.of(page, size, Sort.by("titulo").ascending());

        if (usuario.getRoles().contains(Role.ROLE_ADMIN)) {
            return jogoRepository.findAll(pageable).map(jogoMapper::toResponseDto); // Caso seja um admin pega todos os jogos
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

    /*
        Método privado para realizar a busca por id

        @param id - Identificador único do jogo
    */
    private Jogo buscarPorId(String id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado. ID: " + id));
    }

    /*
        Método privado para buscar avaliações filtradas

        @param jogo - Jogo específico a se buscar avaliações
        @param autores - Usuários autores das avaliações
        @param pageable - Método de paginação
    */
    private List<AvaliacaoResponseDto> buscarReviewsFiltradas(Jogo jogo, List<Usuario> autores, Pageable pageable) {
        if (autores.isEmpty()) {
            return List.of();
        }

        return avaliacaoRepository.findByJogoAndUsuarioIn(jogo, autores, pageable)
                .getContent()
                .stream()
                .map(avaliacaoMapper::toResponseDto)
                .toList();
    }
}