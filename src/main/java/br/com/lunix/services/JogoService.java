package br.com.lunix.services;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.dashboard.DashboardJogoDto;
import br.com.lunix.dto.jogos.*;
import br.com.lunix.dto.rawg.RawgRecords.RawgGameDto;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.AvaliacaoMapper;
import br.com.lunix.mapper.JogoMapper;
import br.com.lunix.mapper.RawgMapper;
import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.PrecoPlataforma;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.AvaliacaoRepository;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
    Serviço responsável por toda a regra de negócio referente aos Jogos.

    Gerencia o ciclo de vida (CRUD), permissões de acesso (Dev vs Admin),
    integrações externas (RAWG e ITAD), mensageria (RabbitMQ) e cache (Redis).
*/
@Service
@RequiredArgsConstructor
public class JogoService {

    private static final Logger log = LoggerFactory.getLogger(JogoService.class);

    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    private final JogoMapper jogoMapper;
    private final RawgMapper rawgMapper;
    private final AvaliacaoMapper avaliacaoMapper;

    private final RawgApiService rawgApiService;
    private final ItadApiService itadApiService;

    private final RabbitTemplate rabbitTemplate;

    @Value("${levelup.rabbitmq.queue}")
    private String queueName;

    /*
        Método responsável por cadastrar um novo jogo na plataforma.

        Verifica se o usuário é ADMIN (pode vincular a qualquer empresa) ou DEV (vincula a si mesmo).
        Aciona o RabbitMQ para buscar preços de forma assíncrona.
        Limpa os caches de listagens públicas (Top 10 e Lançamentos).

        @param requestAdmin - DTO contendo os dados do jogo e possíveis vínculos de empresa/dev.
        return: Retorna o DTO do jogo recém-criado.
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-top10-nota", allEntries = true),
            @CacheEvict(value = "jogos-top10-recentes", allEntries = true)
    })
    public JogoResponseDto cadastrar(JogoAdminRequestDto requestAdmin) {
        // Pega o usuário autenticado no contexto de segurança
        Usuario usuarioLogado = getUsuarioLogado();

        // Converte o DTO base para entidade
        Jogo jogo = jogoMapper.toEntity(requestAdmin.jogoData());

        /*
           Regra de Permissão: Define quem é o dono do jogo.
           Se for ADMIN, pode definir arbitrariamente via DTO.
           Se for DEV, o sistema força o vínculo com o próprio usuário/empresa.
        */
        if (usuarioLogado.getRoles().contains(Role.ROLE_ADMIN)) {
            vincularDonoComoAdmin(jogo, requestAdmin.empresaIdExistente(), null);
        } else {
            vincularDonoComoDev(jogo, usuarioLogado);
        }

        jogo = jogoRepository.save(jogo);

        // Envia mensagem para a fila do RabbitMQ para buscar preços em background
        enviarParaAtualizacaoDePreco(jogo.getId());

        return jogoMapper.toResponseDto(jogo);
    }

    /*
        Busca os detalhes completos de um jogo pelo ID.
        Utiliza Cache (Redis) para evitar consultas repetitivas ao banco.

        @param id - ID do jogo a ser buscado.
        return: DTO detalhado com avaliações separadas por tipo de usuário (Admin, Dev, User).
    */
    @Transactional(readOnly = true)
    @Cacheable(value = "jogos-detalhes", key = "#id")
    public JogoDetalhesDto buscarDetalhesPorId(String id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado. ID: " + id));

        // Busca todas as avaliações do jogo ordenadas por data
        List<Avaliacao> todasAvaliacoes = avaliacaoRepository.findAllByJogoOrderByDataCriacaoDesc(jogo);

        // Filtra e agrupa as avaliações baseadas na Role do autor para exibição organizada
        List<AvaliacaoResponseDto> adminReviews = mapReviewsByRole(todasAvaliacoes, Role.ROLE_ADMIN);
        List<AvaliacaoResponseDto> devReviews = mapReviewsByRole(todasAvaliacoes, Role.ROLE_DEV);
        List<AvaliacaoResponseDto> userReviews = mapReviewsByRole(todasAvaliacoes, Role.ROLE_USER);

        return jogoMapper.toDetalhesDto(jogo, adminReviews, devReviews, userReviews);
    }

    /*
        Lista todos os jogos cadastrados na plataforma de forma paginada.
        Ordenação padrão por data de lançamento (do mais novo para o mais antigo).

        @param page - Número da página.
        @param size - Quantidade de itens por página.
        return: Página com a lista completa de jogos.
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
        return: Página com os jogos disponíveis na plataforma.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorPlataforma(Plataforma plataforma, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByPlataformas(plataforma, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Busca todos os jogos desenvolvidos por uma empresa específica.
        Útil para a página de perfil público da empresa.

        @param empresaId - ID da empresa.
        @param page - Número da página.
        @param size - Quantidade de itens por página.
        return: Página com os jogos daquela empresa.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorEmpresa(String empresaId, int page, int size) {
        // Primeiro verificamos se a empresa existe
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. ID: " + empresaId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByEmpresa(empresa, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Busca todos os jogos desenvolvidos por um desenvolvedor autônomo.
        Útil para o perfil público do Dev.

        @param devId - ID do desenvolvedor.
        @param page - Número da página.
        @param size - Quantidade de itens por página.
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> buscarPorDev(String devId, int page, int size) {
        Usuario dev = usuarioRepository.findById(devId)
                .orElseThrow(() -> new ResourceNotFoundException("Desenvolvedor não encontrado. ID: " + devId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataLancamento").descending());
        return jogoRepository.findByDevAutonomo(dev, pageable).map(jogoMapper::toResponseDto);
    }

    /*
        Busca os 10 jogos com melhor nota média.
        Resultado é armazenado em cache para performance da Home Page.
    */
    @Cacheable(value = "jogos-top10-nota")
    public List<JogoResponseDto> buscarTop10MelhoresAvaliados() {
        return jogoRepository.findTop10ByOrderByNotaMediaDesc().stream()
                .map(jogoMapper::toResponseDto)
                .toList();
    }

    /*
        Busca os 10 jogos lançados mais recentemente.
        Resultado é armazenado em cache.
    */
    @Cacheable(value = "jogos-top10-recentes")
    public List<JogoResponseDto> buscarTop10Lancamentos() {
        return jogoRepository.findTop10ByOrderByDataLancamentoDesc().stream()
                .map(jogoMapper::toResponseDto)
                .toList();
    }

    /*
        Lista apenas os jogos pertencentes ao usuário logado (Área do Dev).
        Se for ADMIN, lista todos os jogos do sistema.

        @param page/size - Paginação.
        return: Página de jogos filtrada pela propriedade (Ownership).
    */
    @Transactional(readOnly = true)
    public Page<JogoResponseDto> listarMeusJogos(int page, int size) {
        Usuario usuario = getUsuarioLogado();
        Pageable pageable = PageRequest.of(page, size, Sort.by("titulo").ascending());

        // Se for ADMIN, vê tudo
        if (usuario.getRoles().contains(Role.ROLE_ADMIN)) {
            return jogoRepository.findAll(pageable).map(jogoMapper::toResponseDto);
        }

        // Se tem empresa vinculada, lista os jogos da empresa. Se não, lista os dele como autônomo.
        if (usuario.getEmpresa() != null) {
            return jogoRepository.findByEmpresa(usuario.getEmpresa(), pageable).map(jogoMapper::toResponseDto);
        } else {
            return jogoRepository.findByDevAutonomo(usuario, pageable).map(jogoMapper::toResponseDto);
        }
    }

    /*
        Gera dados consolidados para o Dashboard administrativo.
        Conta totais, jogos sem preço e distribuição por gênero.
    */
    @Transactional(readOnly = true)
    public DashboardJogoDto gerarDadosDashboard() {
        long totalJogos = jogoRepository.count();

        // Conta jogos onde a lista de preços está vazia (indicando falha de sync ou não encontrado)
        long semPreco = jogoRepository.findByPrecosIsEmpty(Pageable.unpaged()).getTotalElements();

        // Agrega a contagem por gênero
        Map<String, Long> porGenero = new HashMap<>();
        for (Genero g : Genero.values()) {
            long count = jogoRepository.countByGeneros(g);
            if (count > 0) porGenero.put(g.name(), count);
        }

        return new DashboardJogoDto(totalJogos, semPreco, porGenero);
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
        // Busca o jogo e garante que o usuário logado é dono dele
        Jogo jogo = buscarJogoEValidarPermissao(id);

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
        Jogo jogo = buscarJogoEValidarPermissao(id);
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
        Jogo jogo = buscarJogoEValidarPermissao(id);
        jogoRepository.delete(jogo);
    }

    /*
        Publica o ID do jogo na fila do RabbitMQ para processamento assíncrono.
        Utilizado após cadastro ou em atualizações manuais.
    */
    public void enviarParaAtualizacaoDePreco(String jogoId) {
        log.info("Enviando solicitação de atualização de preço para fila: {}", jogoId);
        rabbitTemplate.convertAndSend(queueName, jogoId);
    }

    /*
        Gatilho manual para forçar a atualização de preço de um jogo específico.
        Útil se o preço estiver defasado.
    */
    public void solicitarAtualizacaoManual(String jogoId) {
        if (!jogoRepository.existsById(jogoId)) {
            throw new ResourceNotFoundException("Jogo não encontrado para atualização.");
        }
        enviarParaAtualizacaoDePreco(jogoId);
    }

    /*
        Gatilho administrativo para atualizar preços de TODOS os jogos.
        Cuidado: Pode gerar alta carga na API externa (Rate Limiting).
    */
    @CacheEvict(value = "jogo-detalhes", key = "#jogoId")
    public void solicitarAtualizacaoGlobal() {
        Usuario u = getUsuarioLogado();
        if (!u.getRoles().contains(Role.ROLE_ADMIN)) {
            throw new RegraDeNegocioException("Apenas admin pode disparar atualização global.");
        }

        List<Jogo> todosJogos = jogoRepository.findAll();
        log.info("Disparando atualização para {} jogos.", todosJogos.size());

        todosJogos.forEach(jogo -> rabbitTemplate.convertAndSend(queueName, jogo.getId()));
    }

    /*
        MÉTODO CONSUMIDO PELO LISTENER DO RABBITMQ.

        Realiza a chamada real à API da ITAD e salva os novos preços no banco.

        return: boolean indicando se houve atualização (para controle de Thread.sleep no Consumer).
    */
    @Transactional
    @CacheEvict(value = "jogos-detalhes", key = "#jogoId")
    public boolean processarAtualizacaoDePreco(String jogoId) {
        return jogoRepository.findById(jogoId).map(jogo -> {
            log.info("Processando atualização de preço para: {}", jogo.getTitulo());

            // Busca na API externa via service dedicada
            List<PrecoPlataforma> novosPrecos = itadApiService.buscarPrecosParaJogo(jogo.getTitulo());

            if (!novosPrecos.isEmpty()) {
                jogo.setPrecos(novosPrecos);
                jogo.setUltimaAtualizacaoPrecos(LocalDateTime.now());
                jogoRepository.save(jogo);
                log.info("Preços atualizados para: {}", jogo.getTitulo());
                return true; // Retorna true para sinalizar que houve consumo da API
            } else {
                log.info("Nenhum preço encontrado na ITAD para: {}", jogo.getTitulo());
                return false; // Retorna false para indicar que não precisa esperar (Rate Limit)
            }
        }).orElse(false); // Jogo não encontrado
    }

    /*
        Busca metadados de um jogo na API da RAWG para preencher o formulário automaticamente.

        @param titulo - Nome do jogo a pesquisar.
        return: DTO mapeado com os dados da RAWG prontos para o frontend.
    */
    public JogoMapeadoDto importarDadosRawg(String titulo) {
        List<RawgGameDto> resultados = rawgApiService.buscarJogos(titulo, 1);
        if (resultados.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum jogo indie encontrado na RAWG com o título: " + titulo);
        }
        return rawgMapper.toJogoMapeado(resultados.get(0));
    }

    /*
        Busca um jogo e valida imediatamente se o usuário logado tem permissão para alterá-lo.
        Centraliza a lógica de segurança de acesso aos dados.
    */
    private Jogo buscarJogoEValidarPermissao(String id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado. ID: " + id));
        validarPermissaoEdicao(jogo);
        return jogo;
    }

    /*
        Valida se o usuário logado é dono do jogo (Autônomo ou Empresa) ou Admin.
        Lança RegraDeNegocioException se não tiver permissão.
    */
    private void validarPermissaoEdicao(Jogo jogo) {
        Usuario usuario = getUsuarioLogado();

        // Admin tem passe livre
        if (usuario.getRoles().contains(Role.ROLE_ADMIN)) return;

        // Verifica se é o Dev Autônomo dono do jogo
        boolean isDonoAutonomo = jogo.getDevAutonomo() != null && jogo.getDevAutonomo().getId().equals(usuario.getId());

        // Verifica se a empresa do usuário é a mesma do jogo
        boolean isDonoEmpresa = jogo.getEmpresa() != null && usuario.getEmpresa() != null &&
                jogo.getEmpresa().getId().equals(usuario.getEmpresa().getId());

        if (!isDonoAutonomo && !isDonoEmpresa) {
            throw new RegraDeNegocioException("Você não tem permissão para alterar este jogo.");
        }
    }

    /*
        Lógica para quando um ADMIN cadastra um jogo: ele deve especificar quem é o dono.
    */
    private void vincularDonoComoAdmin(Jogo jogo, String empresaId, String devId) {
        if (empresaId != null && !empresaId.isBlank()) {
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + empresaId));
            jogo.setEmpresa(empresa);
        } else if (devId != null && !devId.isBlank()) {
            Usuario dev = usuarioRepository.findById(devId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dev não encontrado: " + devId));
            jogo.setDevAutonomo(dev);
        } else {
            throw new RegraDeNegocioException("Admin deve informar ID da Empresa para vincular o jogo.");
        }
    }

    /*
        Lógica para quando um DEV cadastra: vincula automaticamente ao seu perfil ou empresa.
    */
    private void vincularDonoComoDev(Jogo jogo, Usuario devLogado) {
        if (devLogado.getEmpresa() != null) {
            jogo.setEmpresa(devLogado.getEmpresa());
        } else {
            jogo.setDevAutonomo(devLogado);
        }
    }

    /*
        Recupera o objeto Usuario completo do contexto de segurança (SecurityContextHolder).
    */
    private Usuario getUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        // Fallback: caso o principal seja apenas o email (String), busca no banco
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado no banco."));
    }

    /*
        Filtra uma lista de avaliações, retornando apenas aquelas feitas por usuários com uma Role específica.
    */
    private List<AvaliacaoResponseDto> mapReviewsByRole(List<Avaliacao> avaliacoes, Role role) {
        return avaliacoes.stream()
                .filter(av -> av.getUsuario().getRoles().contains(role))
                .map(avaliacaoMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}