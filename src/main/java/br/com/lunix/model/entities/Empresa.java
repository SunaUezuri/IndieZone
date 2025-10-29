package br.com.lunix.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


/*
    Entidade do banco de dados que receberá os dados
    das empresas que produziram os jogos

    @Document - Define a classe como uma entidade no MongoDB.
    @Getter @Setter - Cria todos os getters e setters da classe.
    @NoArgsConstructor @AllArgsConstructor - Anotações que criam
    automaticamente os construtores pricipais da classe.
*/
@Document(collection = "empresas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    private String id;

    private String nome;

    private String descricao;

    private String paisOrigem;

    private String urlLogo;

    private LocalDateTime dataCriacao = LocalDateTime.now();
}
