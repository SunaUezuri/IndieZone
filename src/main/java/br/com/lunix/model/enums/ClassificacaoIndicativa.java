package br.com.lunix.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
    Enum com dados de classificação indicativa para o cadastro
    dos jogos na aplicação.

    @param selo - Símbolo referente a classificação indicativa específica.
    @param descricao - Descrição do que significa aquele selo.
    @param corHex - Código Hex de cor para utilização simplificada no front-end
*/
@Getter
@AllArgsConstructor
public enum ClassificacaoIndicativa {
    LIVRE("L", "Livre para todos os públicos", "#008641"),
    DEZ("10", "Não recomendado para menores de 10 anos", "#00A5E3"),
    DOZE("12", "Não recomendado para menores de 12 anos", "#F8A503"),
    CATORZE("14", "Não recomendado para menores de 14 anos", "#E87A1E"),
    DEZESSEIS("16", "Não recomendado para menores de 16 anos", "#C42127"),
    DEZOITO("18", "Não recomendado para menores de 18 anos", "#231F20");

    private final String selo;
    private final String descricao;
    private final String corHex;
}
