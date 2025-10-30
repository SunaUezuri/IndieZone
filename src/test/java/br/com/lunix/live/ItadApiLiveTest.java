package br.com.lunix.live;

import br.com.lunix.model.entities.PrecoPlataforma;
import br.com.lunix.services.ItadApiService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Este teste faz chamadas de rede reais e deve ser executado manualmente.")
public class ItadApiLiveTest {

    @Autowired
    private ItadApiService itadApiService;

    @Test
    public void deveConectarNaApiRealDoItadEBuscarPrecos() {
        // Cenário (Arrange)
        // Escolhemos um jogo popular que certamente terá preços em múltiplas lojas
        String termoBusca = "Hollow Knight: Silksong";

        // Ação (Act)
        List<PrecoPlataforma> resultado = itadApiService.buscarPrecosParaJogo(termoBusca);

        // Verificação (Assert)
        assertThat(resultado).isNotNull().isNotEmpty(); // A verificação principal é que a lista não esteja vazia

        // --- RELATÓRIO DE VOO (IMPRIMINDO TODOS OS RESULTADOS) ---
        System.out.println("========================================================");
        System.out.println("        RELATÓRIO DE PREÇOS - TESTE AO VIVO (ITAD)      ");
        System.out.println("========================================================");
        System.out.printf("Encontradas %d ofertas para '%s':\n\n", resultado.size(), termoBusca);

        for (PrecoPlataforma preco : resultado) {
            System.out.println("  Loja     : " + preco.getNomeLoja());
            System.out.println("  Preço    : " + preco.getPrecoAtual());
            System.out.println("  Base     : " + preco.getPrecoBase());
            System.out.println("  Desconto : " + preco.getDescontoPercentual() + "%");
            System.out.println("  --------------------");
        }
        System.out.println("========================================================");

        // Podemos manter uma verificação simples no primeiro item para garantir a estrutura
        PrecoPlataforma primeiroPreco = resultado.get(0);
        assertThat(primeiroPreco.getNomeLoja()).isNotBlank();
    }
}
