package br.com.lunix.model.entities;

import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
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

    private double notaMedia = 0.0;

    private double totalAvaliacoes = 0;

    @DBRef
    private Empresa empresa;

    @DBRef
    private Usuario devAutonomo;

    private LocalDateTime ultimaAtualizacaoPrecos;

    private LocalDate dataLancamento;

    private LocalDateTime dataCriacao = LocalDateTime.now();


}
