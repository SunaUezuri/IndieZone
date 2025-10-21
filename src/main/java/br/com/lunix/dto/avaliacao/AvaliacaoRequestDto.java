package br.com.lunix.dto.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequestDto(
        @NotNull
        @Min(0)
        @Max(10)
        Integer nota,
        @Size(max = 10000)
        String comentario
) {
}
