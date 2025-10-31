package br.com.lunix.mapper;

import br.com.lunix.dto.itad.ItadRecords.ItadDealDto;
import br.com.lunix.model.entities.PrecoPlataforma;
import org.springframework.stereotype.Component;

/*
    Componente responsável por receber um DTO da API
    ITAD e converter para um objeto do tipo PrecoPlataforma
*/
@Component
public class ItadMapper {

    /*
        Método responsável por fazer a conversão dos dados
        recebidos em um objeto PrecoPlataforma.

        @param dto: Dados da api para serem convertidos.

        @return: Retorna um objeto do tipo PrecoPLataforma.
    */
    public PrecoPlataforma toPrecoPlataforma(ItadDealDto dto) {
        if (dto == null) {
            return null;
        }

        // Caso o campo 'price' esteja vazio o valor fica 0
        double precoAtual = (dto.price() != null) ? dto.price().amount() : 0.0;

        // Caso o campo 'regular' esteja vazio adota o mesmo valor de 'precoAtual'
        double precoBase = (dto.regular() != null) ? dto.regular().amount() : precoAtual;

        return new PrecoPlataforma(
                dto.shop().name(),
                precoAtual,
                precoBase,
                dto.priceCut(),
                dto.url()
        );
    }
}
