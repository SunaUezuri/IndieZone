package br.com.lunix.dto.avaliacao;

import io.swagger.v3.oas.annotations.media.Schema;

// DTO para coletar o resultado das notas dos jogos
public record ResultadoAgregacaoDto(
        @Schema(description = "ID do jogo recuperado", example = "6hbR...")
        String id,
        @Schema(description = "Média que foi calculada para o jogo", example = "9.5")
        Double mediaCalculada,
        @Schema(description = "Quantas avaliações foram feitas", example = "100")
        Integer totalAvaliacoes
) {

}
