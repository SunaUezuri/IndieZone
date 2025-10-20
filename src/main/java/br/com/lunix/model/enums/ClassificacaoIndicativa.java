package br.com.lunix.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
