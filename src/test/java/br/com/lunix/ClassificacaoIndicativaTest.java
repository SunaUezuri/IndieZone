package br.com.lunix;


import br.com.lunix.model.enums.ClassificacaoIndicativa;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassificacaoIndicativaTest {

    @Test
    public void deveRetornarAtributosCorretosParaClassificacaoLivre() {
        ClassificacaoIndicativa classificacao = ClassificacaoIndicativa.LIVRE;

        assertThat(classificacao.getSelo()).isEqualTo("L");
        assertThat(classificacao.getDescricao()).isEqualTo("Livre para todos os públicos");
        assertThat(classificacao.getCorHex()).isEqualTo("#008641");
    }

    @Test
    public void deveRetornarAtributosCorretosParaClassificacaoDez() {
        ClassificacaoIndicativa classificacao = ClassificacaoIndicativa.DEZ;

        assertThat(classificacao.getSelo()).isEqualTo("10");
        assertThat(classificacao.getDescricao()).isEqualTo("Não recomendado para menores de 10 anos");
        assertThat(classificacao.getCorHex()).isEqualTo("#00A5E3");
    }

    @Test
    public void deveRetornarAtributosCorretosParaClassificacaoDoze() {
        ClassificacaoIndicativa classificacao = ClassificacaoIndicativa.DOZE;

        assertThat(classificacao.getSelo()).isEqualTo("12");
        assertThat(classificacao.getDescricao()).isEqualTo("Não recomendado para menores de 12 anos");
        assertThat(classificacao.getCorHex()).isEqualTo("#F8A503");
    }

    @Test
    public void deveRetornarAtributosCorretosParaClassificacaoCatorze() {
        ClassificacaoIndicativa classificacao = ClassificacaoIndicativa.CATORZE;

        assertThat(classificacao.getSelo()).isEqualTo("14");
        assertThat(classificacao.getDescricao()).isEqualTo("Não recomendado para menores de 14 anos");
        assertThat(classificacao.getCorHex()).isEqualTo("#E87A1E");
    }

    @Test
    public void deveRetornarAtributosCorretosParaClassificacaoDezesseis() {
        ClassificacaoIndicativa classificacao = ClassificacaoIndicativa.DEZESSEIS;

        assertThat(classificacao.getSelo()).isEqualTo("16");
        assertThat(classificacao.getDescricao()).isEqualTo("Não recomendado para menores de 16 anos");
        assertThat(classificacao.getCorHex()).isEqualTo("#C42127");
    }

    @Test
    public void deveRetornarAtributosCorretosParaClassificacaoDezoito() {
        ClassificacaoIndicativa classificacao = ClassificacaoIndicativa.DEZOITO;

        assertThat(classificacao.getSelo()).isEqualTo("18");
        assertThat(classificacao.getDescricao()).isEqualTo("Não recomendado para menores de 18 anos");
        assertThat(classificacao.getCorHex()).isEqualTo("#231F20");
    }

    @Test
    public void enumDeveConterTodasAsSeisClassificacoes() {
        assertThat(ClassificacaoIndicativa.values()).hasSize(6);
    }





}
