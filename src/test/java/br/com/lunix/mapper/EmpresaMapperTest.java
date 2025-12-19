package br.com.lunix.mapper;

import br.com.lunix.dto.empresa.EmpresaDetalhesDto;
import br.com.lunix.dto.empresa.EmpresaRequestDto;
import br.com.lunix.dto.empresa.EmpresaResponseDto;
import br.com.lunix.dto.empresa.EmpresaUpdateDto;
import br.com.lunix.dto.jogos.JogoResponseDto;
import br.com.lunix.model.entities.Empresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmpresaMapperTest {

    private final EmpresaMapper mapper = new EmpresaMapper();

    private Empresa empresa;

    @BeforeEach
    void setup() {
       empresa = new Empresa();
    }

    @Test
    @DisplayName("Deve converter EmpresaRequestDto para Entidade corretamente")
    void toEntityDeveConverterDtoParaEntidade() {
        // Cenário
        EmpresaRequestDto dto = new EmpresaRequestDto(
                "Team Cherry",
                "Desenvolvedora Indie",
                "Austrália",
                "http://logo.com"
        );

        // Ação
        Empresa entity = mapper.toEntity(dto);

        // Verificação
        assertThat(entity).isNotNull();
        assertThat(entity.getNome()).isEqualTo(dto.nome());
        assertThat(entity.getDescricao()).isEqualTo(dto.descricao());
        assertThat(entity.getPaisOrigem()).isEqualTo(dto.paisOrigem());
        assertThat(entity.getUrlLogo()).isEqualTo(dto.urlLogo());
    }

    @Test
    @DisplayName("Deve retornar null ao tentar converter RequestDto nulo")
    void toEntityDeveRetornarNullQuandoDtoForNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Deve atualizar os dados da Entidade a partir do UpdateDto")
    void updateEntityFromDtoDeveAtualizarCampos() {
        // Cenário
        empresa.setNome("Nome Antigo");
        empresa.setDescricao("Desc Antiga");
        empresa.setPaisOrigem("Brasil");
        empresa.setUrlLogo("http://antiga.com");

        EmpresaUpdateDto dto = new EmpresaUpdateDto(
                "Nome Novo",
                "Desc Nova",
                "EUA",
                "http://nova.com"
        );

        // Ação
        mapper.updateEntityFromDto(dto, empresa);

        // Verificação
        assertThat(empresa.getNome()).isEqualTo("Nome Novo");
        assertThat(empresa.getDescricao()).isEqualTo("Desc Nova");
        assertThat(empresa.getPaisOrigem()).isEqualTo("EUA");
        assertThat(empresa.getUrlLogo()).isEqualTo("http://nova.com");
    }

    @Test
    @DisplayName("Não deve fazer nada se o UpdateDto for nulo")
    void updateEntityFromDtoNaoDeveFazerNadaSeDtoNull() {
        empresa.setNome("Original");

        mapper.updateEntityFromDto(null, empresa);

        assertThat(empresa.getNome()).isEqualTo("Original");
    }

    @Test
    @DisplayName("Deve converter Entidade para ResponseDto corretamente")
    void toResponseDtoDeveConverterEntidadeParaDto() {
        // Cenário
        empresa.setId("123");
        empresa.setNome("Mojang");
        empresa.setPaisOrigem("Suécia");
        empresa.setUrlLogo("http://mojang.com/logo.png");

        // Ação
        EmpresaResponseDto dto = mapper.toResponseDto(empresa);

        // Verificação
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo("123");
        assertThat(dto.nome()).isEqualTo("Mojang");
        assertThat(dto.paisOrigem()).isEqualTo("Suécia");
        assertThat(dto.urlLogo()).isEqualTo("http://mojang.com/logo.png");
    }

    @Test
    @DisplayName("Deve converter Entidade e Lista de Jogos para DetalhesDto")
    void toDetalhesDtoDeveMapearComJogos() {
        // Cenário
         empresa = new Empresa("1", "Valve", "Desc", "USA", "url", null);

        // Simulando um jogo já convertido
        JogoResponseDto jogoDto = new JogoResponseDto(
                "j1", "Portal", "capa", "Valve", 9.8, Collections.emptyList(), null
        );
        List<JogoResponseDto> listaJogos = List.of(jogoDto);

        // Ação
        EmpresaDetalhesDto dto = mapper.toDetalhesDto(empresa, listaJogos);

        // Verificação
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo("1");
        assertThat(dto.nome()).isEqualTo("Valve");
        assertThat(dto.jogos()).hasSize(1);
        assertThat(dto.jogos().get(0).titulo()).isEqualTo("Portal");
    }
}