package br.com.lunix.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrecoPlataforma {

    private String nomeLoja;

    private double precoAtual;

    private double precoBase;

    private int descontoPercentual;

    private String urlLoja;
}
