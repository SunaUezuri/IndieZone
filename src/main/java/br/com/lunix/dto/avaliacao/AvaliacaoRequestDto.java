package br.com.lunix.dto.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
   DTO utilizado para realizar o cadastro de avaliações para
   jogos.

   @NotNull - Garante que os campos não estejam vazios.
   @Min e @Max - Define um valor mínimo e máximo para a nota.
   @Size - Define um tamanho máximo para o comentário.
*/
public record AvaliacaoRequestDto(
        @NotNull(message = "Não se pode dar uma nota vazia")
        @Min(value = 0, message = "A nota deve ser maior que 0")
        @Max(value = 10, message = "A nota não pode ser mais de 10")
        Integer nota,
        @Size(max = 10000, message = "Número máximo de caracteres ultrapassado(10.000)")
        String comentario
) {
}
