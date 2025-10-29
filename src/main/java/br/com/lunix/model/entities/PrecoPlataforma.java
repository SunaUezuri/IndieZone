package br.com.lunix.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
    Classe de suporte que recebe informações de sobre
    preços de um jogo em diversas lojas

    @Getter @Setter - Cria todos os getters e setters da classe.
    @NoArgsConstructor @AllArgsConstructor - Anotações que criam
    automaticamente os construtores pricipais da classe.
*/
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
