package br.com.lunix.dto.avaliacao;

import br.com.lunix.dto.usuario.UsuarioPublicProfileDto;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
    DTO utilizado para exibir a avaliação na
    página do jogo.
*/
public record AvaliacaoResponseDto(
        String id,
        int nota,
        String comentario,
        LocalDateTime dataCriacao,
        UsuarioPublicProfileDto usuario
) implements Serializable {
}
