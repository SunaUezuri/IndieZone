package br.com.lunix.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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

    private int nota;

    private String comentario;

    private LocalDateTime dataCriacao = LocalDateTime.now();
}
