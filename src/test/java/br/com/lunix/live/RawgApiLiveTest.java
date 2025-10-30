package br.com.lunix.live;

import br.com.lunix.dto.rawg.RawgRecords.RawgGameDto;
import br.com.lunix.services.RawgApiService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ATENÇÃO: Este é um "Smoke Test" de integração REAL para a API RAWG.io.
 * Ele valida o novo fluxo de dados e exibe um relatório detalhado.
 * - Requer uma conexão com a internet.
 * - Requer uma chave de API válida no seu arquivo application.properties.
 * - Está desabilitado por padrão (@Disabled). Execute-o manualmente pela IDE.
 */
@SpringBootTest
@Disabled("Este teste faz uma chamada de rede real e deve ser executado manualmente.")
@TestPropertySource(properties = {
        "mongock.enabled=false"
})
public class RawgApiLiveTest {

    @Autowired
    private RawgApiService rawgApiService;

    @Test
    public void deveConectarNaApiRealDaRawgEBuscarDadosCompletos() {

        String termoBusca = "Hollow Knight";
        int limite = 1;

        List<RawgGameDto> resultado = rawgApiService.buscarJogos(termoBusca, limite);

        assertThat(resultado).isNotNull().isNotEmpty();

        RawgGameDto jogo = resultado.get(0);

        assertThat(jogo.name()).containsIgnoringCase(termoBusca);
        assertThat(jogo.backgroundImage()).isNotNull().startsWith("https://");

        System.out.println("========================================================");
        System.out.println("          RELATÓRIO DE DADOS RAWG - TESTE AO VIVO       ");
        System.out.println("========================================================");
        System.out.printf("Dados encontrados para a busca '%s':\n\n", termoBusca);

        System.out.println("  Título               : " + jogo.name());
        System.out.println("  Slug                 : " + jogo.slug());
        System.out.println("  ID                   : " + jogo.id());
        System.out.println("  Data de Lançamento   : " + (jogo.released() != null ? jogo.released().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"));
        System.out.println("  URL da Capa          : " + jogo.backgroundImage());
        System.out.println("  Descrição            : " + (jogo.description() != null ? jogo.description().substring(0, Math.min(jogo.description().length(), 200)) + "..." : "N/A"));
        String classificacao = jogo.esrbRating() != null ? jogo.esrbRating().name() : "N/A";
        System.out.println("  Classificação ESRB   : " + classificacao);

        // Gêneros
        String generos = jogo.genres() != null ? jogo.genres().stream().map(g -> g.name()).collect(Collectors.joining(", ")) : "N/A";
        System.out.println("  Gêneros              : " + generos);

        // Desenvolvedores
        String desenvolvedores = jogo.developers() != null ? jogo.developers().stream().map(d -> d.name()).collect(Collectors.joining(", ")) : "N/A";
        System.out.println("  Desenvolvedores      : " + desenvolvedores);

        // Plataformas
        String plataformas = jogo.platforms() != null ? jogo.platforms().stream().map(p -> p.platform().name()).collect(Collectors.joining(", ")) : "N/A";
        System.out.println("  Plataformas          : " + plataformas);

        System.out.println("========================================================");
    }
}
