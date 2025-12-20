package br.com.lunix.services.jogo;

import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.PrecoPlataforma;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.services.itad.ItadApiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
    Service responsável por controlar a atualização de preços
    com a API do ITAD.
*/
@Service
@RequiredArgsConstructor
public class JogoPrecoService {

    private static final Logger log = LoggerFactory.getLogger(JogoPrecoService.class);

    private final JogoRepository jogoRepository;
    private final ItadApiService itadApiService;
    private final RabbitTemplate rabbitTemplate;

    private final JogoSecurityService securityService;

    @Value("${indiezone.rabbitmq.queue}")
    private String queueName;

    // Métodos de envio (Producer)

    public void enviarParaFila(String jogoId) {
        log.info("Enviando jogo {} para fila de preços.", jogoId);
        rabbitTemplate.convertAndSend(queueName, jogoId);
    }

    // Método para enviar todos os jogos para a fila
    public void enviarTodosParaFila() {
        List<Jogo> todosJogos = jogoRepository.findAll();
        log.info("Disparando atualização em massa para {} jogos.", todosJogos.size());
        todosJogos.forEach(jogo -> rabbitTemplate.convertAndSend(queueName, jogo.getId()));
    }

    // Método para atualizar todos os jogos manualmente
    @CacheEvict(value = "jogo-detalhes", allEntries = true)
    public void solicitarAtualizacaoGlobalAdmin() {
        var usuario = securityService.getUsuarioLogado();
        // Validação de segurança simplificada
        if (usuario.getRoles().stream().noneMatch(r -> r.name().equals("ROLE_ADMIN"))) {
            throw new RegraDeNegocioException("Apenas admin pode disparar atualização global.");
        }
        enviarTodosParaFila();
    }

    // Métodos de processamento (consumidor)
    @Transactional
    @CacheEvict(value = "jogos-detalhes", key = "#jogoId")
    public boolean processarAtualizacaoLogica(String jogoId) {
        return jogoRepository.findById(jogoId).map(jogo -> {
            log.info("Processando atualização de preço para: {}", jogo.getTitulo());

            List<PrecoPlataforma> novosPrecos = itadApiService.buscarPrecosParaJogo(jogo.getTitulo());

            if (!novosPrecos.isEmpty()) {
                jogo.setPrecos(novosPrecos);
                jogo.setUltimaAtualizacaoPrecos(LocalDateTime.now());
                jogoRepository.save(jogo);
                return true;
            }
            return false;
        }).orElse(false);
    }
}
