package br.com.lunix.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


/*
    Entidade do banco de dados que receberá as avaliações
    feitas de um usuário a um jogo

    @Document - Define a classe como uma entidade no MongoDB.
    @Getter @Setter - Cria todos os getters e setters da classe.
    @NoArgsConstructor @AllArgsConstructor - Anotações que criam
    automaticamente os construtores pricipais da classe.
*/
@Document(collection = "avaliacoes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    private String id;

    @DBRef
    private Usuario usuario;

    @DBRef
    private Jogo jogo;

    private double nota;

    private String comentario;

    private LocalDateTime dataCriacao = LocalDateTime.now();
}
