package br.com.lunix.dto.jogos;

import br.com.lunix.dto.empresa.EmpresaRequestDto;
import jakarta.validation.constraints.NotNull;

public record JogoAdminRequestDto(
        @NotNull
        JogoRequestDto jogoData,
        String empresaIdExistente,
        EmpresaRequestDto novaEmpresaData
) {
}
