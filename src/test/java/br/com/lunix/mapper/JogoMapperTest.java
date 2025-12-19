package br.com.lunix.mapper;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.jogos.*;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JogoMapperTest {

    private JogoMapper mapper;
    private Jogo jogoBase;

    @BeforeEach
    void setUp() {
        mapper = new JogoMapper();

        jogoBase = new Jogo();
        jogoBase.setId("game-1");
        jogoBase.setTitulo("Celeste");
        jogoBase.setDescricao("Escale a montanha");
        jogoBase.setUrlCapa("http://capa.com");
        jogoBase.setUrlTrailer("http://trailer-antigo.com");
        jogoBase.setScreenshots(List.of("screen-antiga-1"));
        jogoBase.setGeneros(List.of(Genero.PLATAFORMA));
        jogoBase.setPlataformas(List.of(Plataforma.PC));
        jogoBase.setClassificacao(ClassificacaoIndicativa.LIVRE);
    }

    @Test
    @DisplayName("Deve converter RequestDto para Entidade e inicializar listas vazias")
    void toEntitySucesso() {
        // Cenário
        JogoRequestDto dto = new JogoRequestDto(
                "Hollow Knight",
                "Insetos e Metroidvania",
                "url-capa",
                LocalDate.of(2017, 2, 24),
                ClassificacaoIndicativa.DEZ,
                List.of(Genero.METROIDVANIA),
                List.of(Plataforma.PC, Plataforma.NINTENDO_SWITCH),
                "url-trailer",
                List.of("screen1", "screen2")
        );

        // Ação
        Jogo entity = mapper.toEntity(dto);

        // Verificação
        assertThat(entity).isNotNull();
        assertThat(entity.getTitulo()).isEqualTo("Hollow Knight");
        assertThat(entity.getNotaMedia()).isEqualTo(0.0); // Verifica valor padrão
        assertThat(entity.getTotalAvaliacoes()).isEqualTo(0); // Verifica valor padrão
        assertThat(entity.getPrecos()).isEmpty(); // Verifica lista vazia padrão
    }

    @Test
    @DisplayName("toEntity deve retornar null se DTO for nulo")
    void toEntityNulo() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Deve atualizar campos quando valores são fornecidos no DTO")
    void updateEntityFromDtoAtualizacaoCompleta() {
        // Cenário: Mudando título e trailer e plataforma para PS5
        JogoUpdateDto dto = new JogoUpdateDto(
                "Celeste: Farewell",
                "Nova descrição",
                "nova-capa",
                LocalDate.now(),
                ClassificacaoIndicativa.DEZ,
                List.of(Genero.AVENTURA),
                List.of(Plataforma.PLAYSTATION_5),
                "http://trailer-novo.com",
                List.of("nova-screen")
        );

        // Ação
        mapper.updateEntityFromDto(dto, jogoBase);

        // Verificação
        assertThat(jogoBase.getTitulo()).isEqualTo("Celeste: Farewell");
        assertThat(jogoBase.getUrlTrailer()).isEqualTo("http://trailer-novo.com");
        assertThat(jogoBase.getPlataformas()).containsExactly(Plataforma.PLAYSTATION_5);
    }

    @Test
    @DisplayName("Não deve atualizar campos opcionais se vierem nulos ou vazios no DTO")
    void updateEntityFromDto_CamposOpcionaisNulos() {
        // Cenário
        JogoUpdateDto dto = new JogoUpdateDto(
                "Celeste 2", // Obrigatório
                "Desc",
                "Capa",
                LocalDate.now(),
                ClassificacaoIndicativa.LIVRE,
                null,
                null,
                null,
                null
        );

        // Ação
        mapper.updateEntityFromDto(dto, jogoBase);

        // Verificação
        assertThat(jogoBase.getTitulo()).isEqualTo("Celeste 2"); // Atualizou obrigatório
        assertThat(jogoBase.getUrlTrailer()).isEqualTo("http://trailer-antigo.com"); // Manteve antigo
        assertThat(jogoBase.getScreenshots()).contains("screen-antiga-1"); // Manteve antiga
        assertThat(jogoBase.getGeneros()).containsExactly(Genero.PLATAFORMA); // Manteve antigo
    }

    @Test
    @DisplayName("Deve atualizar apenas a lista de gêneros")
    void updateGenerosSucesso() {
        JogoGenresPatchDto dto = new JogoGenresPatchDto(List.of(Genero.RPG, Genero.ACAO));

        mapper.updateGeneros(dto, jogoBase);

        assertThat(jogoBase.getGeneros()).containsExactly(Genero.RPG, Genero.ACAO);
    }

    @Test
    @DisplayName("Deve identificar EMPRESA como criadora no ResponseDto")
    void toResponseDtoCriadorEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setNome("Extremely OK Games");
        jogoBase.setEmpresa(empresa);
        jogoBase.setDevAutonomo(null);

        JogoResponseDto dto = mapper.toResponseDto(jogoBase);

        assertThat(dto.nomeCriador()).isEqualTo("Extremely OK Games");
    }

    @Test
    @DisplayName("Deve identificar DEV AUTONOMO como criador no ResponseDto")
    void toResponseDtoCriadorDev() {
        Usuario dev = new Usuario();
        dev.setNome("Maddy Thorson");
        jogoBase.setEmpresa(null);
        jogoBase.setDevAutonomo(dev);

        JogoResponseDto dto = mapper.toResponseDto(jogoBase);

        assertThat(dto.nomeCriador()).isEqualTo("Maddy Thorson");
    }

    @Test
    @DisplayName("Deve retornar 'Desconhecido' se não houver criador vinculado")
    void toResponseDtoCriadorDesconhecido() {
        jogoBase.setEmpresa(null);
        jogoBase.setDevAutonomo(null);

        JogoResponseDto dto = mapper.toResponseDto(jogoBase);

        assertThat(dto.nomeCriador()).isEqualTo("Desconhecido");
    }

    // --- TESTES DE DETALHES (toDetalhesDto) ---

    @Test
    @DisplayName("Deve montar DTO de detalhes tratando listas de reviews nulas")
    void toDetalhesDtoListasNulas() {
        // Cenário
        JogoDetalhesDto dto = mapper.toDetalhesDto(jogoBase, null, null, null);

        // Verificação
        assertThat(dto).isNotNull();
        assertThat(dto.titulo()).isEqualTo(jogoBase.getTitulo());

        assertThat(dto.avaliacoesAdmin()).isNotNull().isEmpty();
        assertThat(dto.avaliacoesDev()).isNotNull().isEmpty();
        assertThat(dto.avaliacoesUsuario()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Deve montar DTO de detalhes com reviews preenchidas")
    void toDetalhesDto_ComReviews() {
        AvaliacaoResponseDto review = new AvaliacaoResponseDto("1", 10.0, "Top", null, null);
        List<AvaliacaoResponseDto> listaUser = List.of(review);

        JogoDetalhesDto dto = mapper.toDetalhesDto(jogoBase, Collections.emptyList(), Collections.emptyList(), listaUser);

        assertThat(dto.avaliacoesUsuario()).hasSize(1);
        assertThat(dto.avaliacoesUsuario().get(0).nota()).isEqualTo(10.0);
    }
}