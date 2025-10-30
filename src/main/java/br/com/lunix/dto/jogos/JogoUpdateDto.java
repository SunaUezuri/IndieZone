package br.com.lunix.dto.jogos;

import br.com.lunix.annotation.interfaces.PastOrPresentDate;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
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
        String titulo,
        @NotBlank(message = "A descricao não pode estar vazia")
        String descricao,
        String urlCapa,
        @NotNull(message = "A data de lançamento é obrigatória")
        @PastOrPresentDate
        LocalDate dataLancamento,
        @NotNull(message = "A classificacao indicativa é obrigatória")
        ClassificacaoIndicativa classificacao,
        @NotEmpty(message = "Um jogo deve ter ao menos um Genero")
        List<Genero> generos,
        @NotEmpty(message = "Selecione ao menos uma plataforma.")
        List<Plataforma> plataformas
) {
}
