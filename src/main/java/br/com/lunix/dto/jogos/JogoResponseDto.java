package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

// DTO de resposta para os jogos
public record JogoResponseDto(
        @Schema(description = "Identificador único do jogo", example = "650c...")
        String id,
        @Schema(description = "Título do jogo", example = "Hollow Knight")
        String titulo,
        @Schema(description = "URL de imagem da capa do jogo", example = "https://img.com")
        String urlCapa,
        @Schema(description = "Nome do criador do jogo", example = "Team Cherry")
        String nomeCriador,
        @Schema(description = "Nota média das avaliações do jogo", example = "9.2")
        double notaMedia,
        @Schema(description = "Lista com os gêneros do jogo", example = "[ 'PLATAFORMA', 'ACAO' ]")
        List<Genero> generos,
        @Schema(description = "Classificação indicativa do jogo", example = "DEZ")
        ClassificacaoIndicativa classificacao
) implements Serializable {
}
