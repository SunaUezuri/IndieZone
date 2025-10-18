package br.com.lunix.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "empresa")
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
}
