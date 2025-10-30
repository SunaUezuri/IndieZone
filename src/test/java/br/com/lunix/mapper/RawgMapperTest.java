package br.com.lunix.mapper;

import br.com.lunix.dto.jogos.JogoMapeadoDto;
import br.com.lunix.dto.rawg.RawgRecords.*;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RawgMapperTest {

    private RawgMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new RawgMapper();
    }

    @Test
    public void deveMapearRawgGameDtoParaJogoMapeadoComSucesso() {
        // Cenário (Arrange) - Criamos um DTO rico com todos os campos que esperamos
        var esrbDto = new RawgEsrbRatingDto(2, "Everyone 10+", "everyone-10-plus");
        var devDto = new RawgDeveloperDto(1, "Team Cherry", "team-cherry");
        var genreDto = new RawgGenreDto(1, "Adventure", "adventure");
        var platformDto = new RawgPlatformEntryDto(new RawgPlatformDto(4, "PC", "pc"));
        var rawgGameDto = new RawgGameDto(123, "hollow-knight", "Hollow Knight", LocalDate.of(2017, 2, 24),
                "url/capa.jpg", esrbDto, List.of(platformDto), List.of(genreDto), List.of(devDto), ""
        );

        // Ação (Act)
        JogoMapeadoDto resultado = mapper.toJogoMapeado(rawgGameDto);

        // Verificação (Assert)
        assertThat(resultado).isNotNull();

        // Verifica o nome do desenvolvedor
        assertThat(resultado.nomeDesenvolvedorPrincipal()).isEqualTo("Team Cherry");

        // Verifica os campos do jogo
        Jogo jogo = resultado.jogo();
        assertThat(jogo).isNotNull();
        assertThat(jogo.getTitulo()).isEqualTo("Hollow Knight");
        assertThat(jogo.getUrlCapa()).isEqualTo("url/capa.jpg");
        assertThat(jogo.getDataLancamento()).isEqualTo(LocalDate.of(2017, 2, 24));
        assertThat(jogo.getGeneros()).containsExactly(Genero.AVENTURA);
        assertThat(jogo.getPlataformas()).containsExactly(Plataforma.PC);
        assertThat(jogo.getClassificacao()).isEqualTo(ClassificacaoIndicativa.DEZ);
    }

    @Test
    public void deveLidarComListasNulasOuVaziasCorretamente() {
        // Cenário com listas nulas e vazias
        var rawgGameDto = new RawgGameDto(
                123, "slug", "Jogo Simples", null, null, null,
                null,
                List.of(),
                null,
                ""
        );

        // Ação
        JogoMapeadoDto resultado = mapper.toJogoMapeado(rawgGameDto);

        // Verificação
        assertThat(resultado).isNotNull();
        assertThat(resultado.jogo().getPlataformas()).isNotNull().isEmpty();
        assertThat(resultado.jogo().getGeneros()).isNotNull().isEmpty();
        assertThat(resultado.nomeDesenvolvedorPrincipal()).isNull();
    }

    @Test
    public void deveMapearSlugsDesconhecidosParaOutros() {
        var genreDto = new RawgGenreDto(99, "Music", "music");
        var platformDto = new RawgPlatformEntryDto(new RawgPlatformDto(99, "Atari 2600", "atari-2600"));
        var rawgGameDto = new RawgGameDto(
                456, "slug", "Jogo Legado", null, null, null,
                List.of(platformDto), List.of(genreDto), List.of(), null
        );

        // Ação
        JogoMapeadoDto resultado = mapper.toJogoMapeado(rawgGameDto);

        // Verificação
        assertThat(resultado).isNotNull();
        // Garante que nossa rede de segurança 'default' está funcionando
        assertThat(resultado.jogo().getGeneros()).containsExactly(Genero.OUTROS);
        assertThat(resultado.jogo().getPlataformas()).containsExactly(Plataforma.OUTROS);
    }

    @Test
    public void deveRetornarNuloQuandoDtoForNulo() {
        assertThat(mapper.toJogoMapeado(null)).isNull();
    }
}
