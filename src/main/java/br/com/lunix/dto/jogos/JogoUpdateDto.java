package br.com.lunix.dto.jogos;

import br.com.lunix.annotation.interfaces.PastOrPresentDate;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record JogoUpdateDto(
        @NotBlank
        String titulo,
        @NotBlank
        String descricao,
        String urlCapa,
        @NotNull
        @PastOrPresentDate
        LocalDate dataLancamento,
        @NotNull
        ClassificacaoIndicativa classificacao,
        @NotEmpty
        List<Genero> generos
) {
}
