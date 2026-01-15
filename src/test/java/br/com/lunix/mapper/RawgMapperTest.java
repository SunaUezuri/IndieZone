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
        // Cenário (Arrange)
        var esrbDto = new RawgEsrbRatingDto(2, "Everyone 10+", "everyone-10-plus");
        var devDto = new RawgDeveloperDto(1, "Team Cherry", "team-cherry");
        var genreDto = new RawgGenreDto(1, "Adventure", "adventure");
        var platformDto = new RawgPlatformEntryDto(new RawgPlatformDto(4, "PC", "pc"));

        var screenshotDto = new RawgScreenshotDto(100, "https://img.com/screen1.jpg");
        var clipDto = new RawgClipDto("https://video.com/trailer.mp4", "video", "preview");

        var rawgGameDto = new RawgGameDto(
                123,
                "hollow-knight",
                "Hollow Knight",
                LocalDate.of(2017, 2, 24),
                "url/capa.jpg",
                esrbDto,
                List.of(platformDto),
                List.of(genreDto),
                List.of(devDto),
                "Descrição do jogo",
                List.of(screenshotDto),
                clipDto
        );

        // Ação (Act)
        JogoMapeadoDto resultado = mapper.toJogoMapeado(rawgGameDto);

        // Verificação (Assert)
        assertThat(resultado).isNotNull();

        // Verifica o nome do desenvolvedor
        assertThat(resultado.nomeDesenvolvedorPrincipal()).isEqualTo("Team Cherry");

        // Verifica os campos do jogo
        assertThat(resultado.titulo()).isEqualTo("Hollow Knight");
        assertThat(resultado.urlCapa()).isEqualTo("url/capa.jpg");
        assertThat(resultado.dataLancamento()).isEqualTo(LocalDate.of(2017, 2, 24));
        assertThat(resultado.generos()).containsExactly(Genero.AVENTURA);
        assertThat(resultado.plataformas()).containsExactly(Plataforma.PC);
        assertThat(resultado.classificacao()).isEqualTo(ClassificacaoIndicativa.DEZ);
        assertThat(resultado.screenshots()).hasSize(1);
        assertThat(resultado.screenshots().get(0)).isEqualTo("https://img.com/screen1.jpg");
        assertThat(resultado.urlTrailer()).isEqualTo("https://video.com/trailer.mp4");
    }

    @Test
    public void deveLidarComListasNulasOuVaziasCorretamente() {
        // Cenário com listas nulas e vazias
        // Passamos null para screenshots e clip também
        var rawgGameDto = new RawgGameDto(
                123, "slug", "Jogo Simples", null, null, null,
                null,
                List.of(),
                null,
                "",
                null,
                null
        );

        // Ação
        JogoMapeadoDto resultado = mapper.toJogoMapeado(rawgGameDto);

        // Verificação
        assertThat(resultado).isNotNull();
        assertThat(resultado.plataformas()).isNotNull().isEmpty();
        assertThat(resultado.generos()).isNotNull().isEmpty();
        assertThat(resultado.nomeDesenvolvedorPrincipal()).isNull();

        assertThat(resultado.screenshots()).isNullOrEmpty();
        assertThat(resultado.urlTrailer()).isNull();
    }

    @Test
    public void deveMapearSlugsDesconhecidosParaOutros() {
        var genreDto = new RawgGenreDto(99, "Music", "music");
        var platformDto = new RawgPlatformEntryDto(new RawgPlatformDto(99, "Atari 2600", "atari-2600"));

        // Passamos listas vazias ou nulas para os novos campos, pois o foco aqui é Enum
        var rawgGameDto = new RawgGameDto(
                456, "slug", "Jogo Legado", null, null, null,
                List.of(platformDto), List.of(genreDto), List.of(), null,
                null,
                null
        );

        // Ação
        JogoMapeadoDto resultado = mapper.toJogoMapeado(rawgGameDto);

        // Verificação
        assertThat(resultado).isNotNull();
        // Garante que nossa rede de segurança 'default' está funcionando
        assertThat(resultado.generos()).containsExactly(Genero.OUTROS);
        assertThat(resultado.plataformas()).containsExactly(Plataforma.OUTROS);
    }

    @Test
    public void deveRetornarNuloQuandoDtoForNulo() {
        assertThat(mapper.toJogoMapeado(null)).isNull();
    }
}