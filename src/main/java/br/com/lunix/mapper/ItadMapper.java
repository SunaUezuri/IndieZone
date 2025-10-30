package br.com.lunix.mapper;

import br.com.lunix.dto.itad.ItadRecords.ItadDealDto;
import br.com.lunix.model.entities.PrecoPlataforma;
import org.springframework.stereotype.Component;

@Component
public class ItadMapper {

    public PrecoPlataforma toPrecoPlataforma(ItadDealDto dto) {
        if (dto == null) {
            return null;
        }

        double precoAtual = (dto.price() != null) ? dto.price().amount() : 0.0;
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
