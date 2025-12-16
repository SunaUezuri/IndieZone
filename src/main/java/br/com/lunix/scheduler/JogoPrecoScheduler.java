package br.com.lunix.scheduler;

import br.com.lunix.services.jogo.JogoPrecoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JogoPrecoScheduler {

    private static final Logger log = LoggerFactory.getLogger(JogoPrecoScheduler.class);

    private final JogoPrecoService precoService;

    /*
        Executa a atualização de preços automaticamente.
        Cron Pattern: Seg Min Hora Dia Mes DiaSemana
    */

    // Configurado para rodar todos os dias às 03:00 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void agendarAtualizacaoDiaria() {
        log.info("SCHEDULER: Acordando para atualizar preços dos jogos...");
        precoService.enviarTodosParaFila();
    }
}
