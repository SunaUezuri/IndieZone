package br.com.lunix.mapper;

import br.com.lunix.dto.itad.ItadRecords.*;
import br.com.lunix.model.entities.PrecoPlataforma;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ItadMapperTest {

    private final ItadMapper mapper = new ItadMapper();

    @Test
    public void deveConverterDtoParaPrecoPlataformaComSucesso() {
        // Cenário (Arrange) - ATUALIZADO para a nova estrutura de DTO
        var shopDto = new ItadShopDto(1, "Steam");
        var priceDetails = new ItadPriceDetailsDto(9.99, "BRL"); // Novo DTO
        var regularDetails = new ItadPriceDetailsDto(19.99, "BRL"); // Novo DTO
        var dealDto = new ItadDealDto(shopDto, priceDetails, regularDetails, 50, "url/steam", null);

        // Ação (Act)
        PrecoPlataforma resultado = mapper.toPrecoPlataforma(dealDto);

        // Verificação (Assert)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNomeLoja()).isEqualTo("Steam");
        assertThat(resultado.getPrecoAtual()).isEqualTo(9.99);
        assertThat(resultado.getPrecoBase()).isEqualTo(19.99);
    }

    @Test
    public void deveRetornarNuloQuandoDtoForNulo() {
        assertThat(mapper.toPrecoPlataforma(null)).isNull();
    }
}