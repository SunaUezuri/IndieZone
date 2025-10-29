package br.com.lunix.mapper;

import br.com.lunix.dto.itad.ItadRecords.ItadPriceEntryDto;
import br.com.lunix.model.entities.PrecoPlataforma;
import org.springframework.stereotype.Component;

@Component
public class ItadMapper {

    public PrecoPlataforma toPrecoPlataforma(ItadPriceEntryDto dto) {
        if (dto == null) {
            return null;
        }

        return new PrecoPlataforma(
                dto.shop().name(),
                dto.precoAtual(),
                dto.precoBase(),
                dto.descontoPercentual(),
                dto.urlLoja()
        );
    }
}
