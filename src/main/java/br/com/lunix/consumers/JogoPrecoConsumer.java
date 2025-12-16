package br.com.lunix.consumers;

import br.com.lunix.services.jogo.JogoPrecoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JogoPrecoConsumer {

    private static final Logger log = LoggerFactory.getLogger(JogoPrecoConsumer.class);

    private final JogoPrecoService precoService;

    /*
        Método que ouve a fila de atualização de preços

        @param jogoId - ID do jogo a ser atualizado
    */
    @RabbitListener(queues = "${indiezone.rabbitmq.queue}")
    public void consumirMensagem(String jogoId) {
        log.info("Mensagem recebida da fila. Iniciando atualização para Jogo ID: {}", jogoId);

        try {
            // A regra de negócio é delegada para a Service
            boolean atualizou = precoService.processarAtualizacaoLogica(jogoId);

            if (atualizou) {
                // Rate limiting: Pausa estratégica para respeitar a API do ITAD
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Consumer interrompido inesperadamente", e);
        } catch (Exception e) {
            // Loga o erro mas não derruba o listener.
            log.error("Erro ao processar atualização de preço para o ID: {}", jogoId, e);
        }
    }
}
