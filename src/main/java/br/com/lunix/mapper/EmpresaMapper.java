package br.com.lunix.mapper;

import br.com.lunix.dto.empresa.EmpresaDetalhesDto;
import br.com.lunix.dto.empresa.EmpresaRequestDto;
import br.com.lunix.dto.empresa.EmpresaResponseDto;
import br.com.lunix.dto.empresa.EmpresaUpdateDto;
import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.model.entities.Empresa;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmpresaMapper {

    /*
      Converte o DTO de criação para a Entidade Empresa.
    */
    public Empresa toEntity(EmpresaRequestDto dto) {
        if (dto == null) return null;

        Empresa empresa = new Empresa();
        empresa.setNome(dto.nome());
        empresa.setDescricao(dto.descricao());
        empresa.setPaisOrigem(dto.paisOrigem());
        empresa.setUrlLogo(dto.urlLogo());

        return empresa;
    }

    /*
      Atualiza uma empresa existente com os dados do formulário de update.
    */
    public void updateEntityFromDto(EmpresaUpdateDto dto, Empresa empresa) {
        if (dto == null || empresa == null) return;

        empresa.setNome(dto.nome());
        empresa.setDescricao(dto.descricao());
        empresa.setPaisOrigem(dto.paisOrigem());

        empresa.setUrlLogo(dto.urlLogo());
    }

    /*
      Retorna o DTO padrão de resposta (para listas, selects, etc).
    */
    public EmpresaResponseDto toResponseDto(Empresa empresa) {
        if (empresa == null) return null;

        return new EmpresaResponseDto(
                empresa.getId(),
                empresa.getNome(),
                empresa.getPaisOrigem(),
                empresa.getUrlLogo()
        );
    }

    /*
      Monta o DTO de detalhes, que inclui a empresa e a lista de jogos dela.
    */
    public EmpresaDetalhesDto toDetalhesDto(Empresa empresa, List<JogoResponseDto> jogos) {
        if (empresa == null) return null;

        return new EmpresaDetalhesDto(
                empresa.getId(),
                empresa.getNome(),
                empresa.getDescricao(),
                empresa.getPaisOrigem(),
                empresa.getUrlLogo(),
                jogos
        );
    }
}
