package br.com.lunix.dto.jogos;

import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/*
    DTO responsável por receber o objeto mapeado
    pelo RawgMapper.
*/
public record JogoMapeadoDto(
        @Schema(description = "Título do jogo", example = "Hollow Knight")
        String titulo,

        @Schema(description = "Descrição do jogo", example = "Venha se abrigar em hallownest")
        String descricao,

        @Schema(description = "URL de imagem da capa do jogo")
        String urlCapa,

        @Schema(description = "URL do trailer")
        String urlTrailer,

        @Schema(description = "Lista de screenshots")
        List<String> screenshots,

        @Schema(description = "Data de lançamento")
        LocalDate dataLancamento,

        @Schema(description = "Classificação indicativa sugerida")
        ClassificacaoIndicativa classificacao,

        @Schema(description = "Lista de gêneros mapeados")
        List<Genero> generos,

        @Schema(description = "Lista de plataformas mapeadas")
        List<Plataforma> plataformas,
        @Schema(description = "Nome do desenvolvedor", example = "Team Cherry")
        String nomeDesenvolvedorPrincipal
) {
}
