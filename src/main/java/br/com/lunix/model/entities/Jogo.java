package br.com.lunix.model.entities;

import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/*
    Entidade do banco de dados que receberá dados dos jogos
    que estarãp presentes na aplicação.

    @Document - Define a classe como uma entidade no MongoDB.
    @Getter @Setter - Cria todos os getters e setters da classe.
    @NoArgsConstructor @AllArgsConstructor - Anotações que criam
    automaticamente os construtores pricipais da classe.
*/
@Document(collection = "jogos")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Jogo {

    @Id
    private String id;

    private String titulo;

    private String descricao;

    private String urlCapa;

    private ClassificacaoIndicativa classificacao;

    private List<Genero> generos = new ArrayList<>();

    private List<PrecoPlataforma> precos = new ArrayList<>();

    private List<Plataforma> plataformas = new ArrayList<>();

    private double notaMedia = 0.0;

    private int totalAvaliacoes = 0;

    @DBRef
    private Empresa empresa;

    @DBRef
    private Usuario devAutonomo;

    private LocalDateTime ultimaAtualizacaoPrecos;

    private LocalDate dataLancamento;

    private LocalDateTime dataCriacao = LocalDateTime.now();


}
