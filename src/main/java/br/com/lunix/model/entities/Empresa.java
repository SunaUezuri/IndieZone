package br.com.lunix.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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
