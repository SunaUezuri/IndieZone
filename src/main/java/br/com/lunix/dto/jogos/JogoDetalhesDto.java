package br.com.lunix.dto.jogos;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.PrecoPlataforma;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

// Record para exibir os detalhes sobre um jogo.
public record JogoDetalhesDto(
        @Schema(description = "Identificador único do jogo", example = "650c...")
        String id,
        @Schema(description = "Título do jogo", example = "Hollow Knight")
        String titulo,
        @Schema(description = "Descrição do jogo", example = "Venha se abrigar em hallownest")
        String descricao,
        @Schema(description = "URL de imagem da capa do jogo", example = "https://img.com")
        String urlCapa,
        @Schema(description = "Data de lançamento do jogo", example = "2022-12-16")
        LocalDate dataLancamento,
        @Schema(description = "Classificação indicativa do jogo", example = "DEZ")
        ClassificacaoIndicativa classificacao,
        @Schema(description = "Lista com os gêneros do jogo", example = "[ 'PLATAFORMA', 'ACAO' ]")
        List<Genero> generos,
        @Schema(description = "Lista de lojas com o preço dos jogos nela")
        List<PrecoPlataforma> precos,
        @Schema(description = "Lista de plataformas que se dá para jogar", example = "[ 'PC', 'SWITCH' ]")
        List<Plataforma> plataformas,
        @Schema(description = "Nota média das avaliações do jogo", example = "9.2")
        double notaMedia,
        @Schema(description = "Quantidade total de avaliações do jogo", example = "40")
        int totalAvaliacoes,
        @Schema(description = "Nome do criador do jogo", example = "Team Cherry")
        String nomeCriador,
        @Schema(description = "URL do trailer (Youtube/Clip)", example = "https://video.com/clip.mp4")
        String urlTrailer,
        @Schema(description = "Lista de URLs de screenshots", example = "['http://img1.com', 'http://img2.com']")
        List<String> screenshots,
        @Schema(description = "Lista de avaliações dos administradores")
        List<AvaliacaoResponseDto> avaliacoesAdmin,
        @Schema(description = "Lista de avaliações dos usuários")
        List<AvaliacaoResponseDto> avaliacoesUsuario,
        @Schema(description = "Lista de avaliações de devs")
        List<AvaliacaoResponseDto> avaliacoesDev
) implements Serializable {
}
