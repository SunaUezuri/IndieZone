package br.com.lunix.services.avaliacao;

import br.com.lunix.dto.avaliacao.AvaliacaoRequestDto;
import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.avaliacao.AvaliacaoUpdateDto;
import br.com.lunix.dto.avaliacao.ResultadoAgregacaoDto;
import br.com.lunix.exceptions.AutoAvaliacaoException;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.AvaliacaoMapper;
import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.AvaliacaoRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.services.jogo.JogoSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository repository;
    private final JogoRepository jogoRepository;

    private final AvaliacaoMapper mapper;
    private final JogoSecurityService securityService;

    /*
        Método de criação, valida a autenticidade da criação e atualiza as estatísticas
        do jogo avaliado.

        @param jogoId - Identificador único do jogo sendo avaliado
        @param dto - Dados de avaliação
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-detalhes", key = "#jogoId"),
            @CacheEvict(value = "jogos-top10-nota", allEntries = true)
    })
    public AvaliacaoResponseDto criar(String jogoId, AvaliacaoRequestDto dto) {
        Usuario usuario = securityService.getUsuarioLogado();
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado com o ID: " + jogoId));

        // Regra de unicidade
        if (repository.existsByUsuarioAndJogo(usuario, jogo)) {
            throw new RegraDeNegocioException("Você já avaliou este jogo. Edite sua avaliação existente.");
        }

        // Valida a integridade (Dev não pode avaliar o próprio jogo)
        validarIntegridade(usuario, jogo);

        Avaliacao avaliacao = mapper.toEntity(dto);
        avaliacao.setUsuario(usuario);
        avaliacao.setJogo(jogo);

        avaliacao = repository.save(avaliacao);

        // Atualiza a média com o repository customizado
        atualizarEstatiscasDoJogo(jogo);

        return mapper.toResponseDto(avaliacao);
    }

    /*
        Método de atualização de comentário (Só o autor pode atualizar o comentário)

        @param jogoId - Identificador único do jogo sendo avaliado
        @param dto - Dados de avaliação
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-detalhes", allEntries = true),
            @CacheEvict(value = "jogos-top10-nota", allEntries = true)
    })
    public AvaliacaoResponseDto atualizar(String id, AvaliacaoUpdateDto dto) {
        Avaliacao avaliacao = buscarPorIdEntidade(id);
        Usuario usuario = securityService.getUsuarioLogado();

        // Somente o autor pode editar a avaliação
        if (!avaliacao.getUsuario().getId().equals(usuario.getId())) {
            throw new RegraDeNegocioException("Você não tem permissão para editar esta avaliação.");
        }

        mapper.updateFromEntityDto(dto, avaliacao);
        avaliacao = repository.save(avaliacao);

        atualizarEstatiscasDoJogo(avaliacao.getJogo());

        return mapper.toResponseDto(avaliacao);
    }

    /*
        Método de deleção de comentários (Apenas admin ou o autor podem apagar)

        @param id - ID do jogo a ser deletado
    */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "jogos-detalhes", allEntries = true),
            @CacheEvict(value = "jogos-top10-nota", allEntries = true)
    })
    public void deletar(String id) {
        Avaliacao avaliacao = buscarPorIdEntidade(id);
        Usuario usuario = securityService.getUsuarioLogado();

        // Apenas autor ou admin podem deletar
        boolean isDono = avaliacao.getUsuario().getId().equals(usuario.getId());
        boolean isAdmin = usuario.getRoles().contains(Role.ROLE_ADMIN);

        if (!isDono && !isAdmin) {
            throw new RegraDeNegocioException("Você não tem permissão para deletar esta avaliação.");
        }

        Jogo jogoAlvo = avaliacao.getJogo();
        repository.delete(avaliacao);

        atualizarEstatiscasDoJogo(jogoAlvo);
    }

    /*
        Método para listar todos as avaliações feitas
        para um jogo

        @param jogoId - jogo que possuí as avaliações
        @param page - Número da página
        @param size - Número de itens a se ter na página
    */
    @Transactional(readOnly = true)
    public Page<AvaliacaoResponseDto> listarPorJogo(String jogoId, int page, int size) {
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado."));

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataCriacao").descending());
        return repository.findByJogo(jogo, pageable).map(mapper::toResponseDto);
    }

    /*
        Método que lista todas as avaliações feitas por um usuário

        @param page - Número da página
        @param size - Número de itens a se ter na página
    */
    @Transactional(readOnly = true)
    public Page<AvaliacaoResponseDto> listarMinhas(int page, int size) {
        Usuario usuario = securityService.getUsuarioLogado();
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataCriacao").descending());
        return repository.findByUsuario(usuario, pageable).map(mapper::toResponseDto);
    }

    /*
        Método privado para validar se o autor da avaliação está
        relacionado ao Jogo, seja como Dev ou pois faz parte da empresa.

        @param usuario - Usuario que está logado
        @param jogo - Jogo sendo avaliado
    */
    private void validarIntegridade(Usuario usuario, Jogo jogo) {
        if (jogo.getDevAutonomo() != null && jogo.getDevAutonomo().getId().equals(usuario.getId())) {
            throw new AutoAvaliacaoException("Você é o criador deste jogo e não pode avaliá-lo.");
        }
        if (jogo.getEmpresa() != null && usuario.getEmpresa() != null) {
            if (jogo.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                throw new AutoAvaliacaoException("Você faz parte da empresa desenvolvedora e não pode avaliar este jogo.");
            }
        }
    }

    /*
        Busca a entidade de avaliação do sistema

        @param id - ID da avaliação
    */
    private Avaliacao buscarPorIdEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada. ID: " + id));
    }

    /*
        Método para chamar o repositório customizado para calcular a média
        de forma performática e atualiza a entidade de jogo.

        @param jogo - Jogo alvo
    */
    private void atualizarEstatiscasDoJogo(Jogo jogo) {
        ResultadoAgregacaoDto dados = repository.calcularMediaDoJogo(jogo.getId());

        if (dados != null) {
            System.out.println("Agregação SUCESSO: Média " + dados.mediaCalculada() + " | Total " + dados.totalAvaliacoes());
            // Arredonda para 1 casa decimal
            double mediaArredondada = Math.round(dados.mediaCalculada() * 10.0) / 10.0;

            jogo.setNotaMedia(mediaArredondada);
            jogo.setTotalAvaliacoes(dados.totalAvaliacoes());
        } else {
            System.out.println("Agregação retornou NULL para o jogo: " + jogo.getTitulo());
            jogo.setNotaMedia(0.0);
            jogo.setTotalAvaliacoes(0);
        }

        jogoRepository.save(jogo);
    }
}
