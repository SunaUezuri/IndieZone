package br.com.lunix.mapper;

import br.com.lunix.dto.itad.ItadRecords.ItadPriceEntryDto;
import br.com.lunix.dto.itad.ItadRecords.ItadShopDto;
import br.com.lunix.model.entities.PrecoPlataforma;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ItadMapperTest {

    private final ItadMapper itadMapper = new ItadMapper();

    @Test
    public void deveConverterDtoParaPrecoPlataformaComSucesso() {

        var shopDto = new ItadShopDto("Steam");
        var priceEntryDto = new ItadPriceEntryDto(
                shopDto,
                49.90,
                99.80,
                50,
                "https://store.steampowered.com/app/12345"
        );

        PrecoPlataforma resultado = itadMapper.toPrecoPlataforma(priceEntryDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNomeLoja()).isEqualTo("Steam");
        assertThat(resultado.getPrecoAtual()).isEqualTo(49.90);
        assertThat(resultado.getPrecoBase()).isEqualTo(99.80);
        assertThat(resultado.getDescontoPercentual()).isEqualTo(50);
        assertThat(resultado.getUrlLoja()).isEqualTo("https://store.steampowered.com/app/12345");
    }

    @Test
    public void quandoDtoForNuloDeveRetornarNulo() {

        ItadPriceEntryDto dtoNulo = null;

        PrecoPlataforma resultado = itadMapper.toPrecoPlataforma(dtoNulo);

        assertThat(resultado).isNull();
    }
}
