package br.com.lunix.dto.avaliacao;

import br.com.lunix.dto.usuario.UsuarioPublicProfileDto;

import java.time.LocalDateTime;

public record AvaliacaoResponseDto(
        String id,
        int nota,
        String comentario,
        LocalDateTime dataCriacao,
        UsuarioPublicProfileDto usuario
) {
}
