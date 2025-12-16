package br.com.lunix.dto.jogos;

import br.com.lunix.annotation.interfaces.PastOrPresentDate;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/*
   DTO utilizado para realizar o update de jogos.

   @NotBlank, @NotNull e @NotEmpty - Garante que os campos não estejam vazios.
   @PastOrPresentDate - Garante que uma data futura não seja inserida.
*/
public record JogoUpdateDto(
        @NotBlank(message = "O título não pode estar vazio")
        @Schema(description = "Título do jogo", example = "Hollow Knight")
        String titulo,
        @NotBlank(message = "A descricao não pode estar vazia")
        @Schema(description = "Descrição do jogo", example = "Venha se abrigar em hallownest")
        String descricao,
        @Schema(description = "URL de imagem da capa do jogo", example = "https://img.com")
        String urlCapa,
        @NotNull(message = "A data de lançamento é obrigatória")
        @PastOrPresentDate
        @Schema(description = "Data de lançamento do jogo", example = "2022-12-16")
        LocalDate dataLancamento,
        @NotNull(message = "A classificacao indicativa é obrigatória")
        @Schema(description = "Classificação indicativa do jogo", example = "DEZ")
        ClassificacaoIndicativa classificacao,
        @NotEmpty(message = "Um jogo deve ter ao menos um Genero")
        @Schema(description = "Lista com os gêneros do jogo", example = "[ 'PLATAFORMA', 'ACAO' ]")
        List<Genero> generos,
        @NotEmpty(message = "Selecione ao menos uma plataforma.")
        @Schema(description = "Lista de plataformas que se dá para jogar", example = "[ 'PC', 'SWITCH' ]")
        List<Plataforma> plataformas,
        @Schema(description = "URL do trailer (Youtube/Clip)", example = "https://video.com/clip.mp4")
        String urlTrailer,
        @Schema(description = "Lista de URLs de screenshots", example = "['http://img1.com', 'http://img2.com']")
        List<String> screenshots
) {
}
