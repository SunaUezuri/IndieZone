package br.com.lunix.dto.avaliacao;

import br.com.lunix.dto.usuario.UsuarioPublicProfileDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
    DTO utilizado para exibir a avaliação na
    página do jogo.
*/
public record AvaliacaoResponseDto(
        @Schema(description = "Identificador único da avaliação", example = "650c...")
        String id,
        @Schema(description = "Nota dada na avaliação", example = "8.5")
        Double nota,
        @Schema(description = "Comentário inserido pelo usuário")
        String comentario,
        @Schema(description = "Data em que a avaliação foi postada", example = "2025-12-16")
        LocalDateTime dataCriacao,
        @Schema(description = "Usuário que realizou a avaliação")
        UsuarioPublicProfileDto usuario
) implements Serializable {
}
