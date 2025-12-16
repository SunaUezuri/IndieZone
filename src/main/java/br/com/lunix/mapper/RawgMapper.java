package br.com.lunix.mapper;

import br.com.lunix.dto.jogos.JogoMapeadoDto;
import br.com.lunix.dto.rawg.RawgRecords.*;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.enums.ClassificacaoIndicativa;
import br.com.lunix.model.enums.Genero;
import br.com.lunix.model.enums.Plataforma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/*
    Componente responsável por realizar o mapeamento
    de dados vindos da API da RAWG em entidades da aplicação.

    Possui métodos de tradução de dados da API para ENUMS,
    transformação de dados em listas dentre outros
*/
@Component
public class RawgMapper {

    private static final Logger log = LoggerFactory.getLogger(RawgMapper.class);

    /*
        Método principal que pega todos os dados encontrados
        e os transforma em uma entidade JogoMapeadoDto.

        @param dto: Resposta da api a ser transformada.
        @return: Retorna um record com o jogo mapeado.
    */
    public JogoMapeadoDto toJogoMapeado(RawgGameDto dto) {
        if (dto == null) {
            return null;
        }

        Jogo jogo = new Jogo();
        jogo.setTitulo(dto.name());
        jogo.setDescricao(dto.description());
        jogo.setDataLancamento(dto.released());
        jogo.setUrlCapa(dto.backgroundImage());

        if (dto.shortScreenshots() != null) {
            List<String> urls = dto.shortScreenshots().stream()
                    .map(RawgScreenshotDto::image) // Pega só a URL da imagem
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            jogo.setScreenshots(urls);
        }

        if (dto.clip() != null && dto.clip().clip() != null) {
            jogo.setUrlTrailer(dto.clip().clip()); // Pega a URL do vídeo (SD ou HD)
        }

        // Mapeia as listas, traduzindo os valores da API para nossos Enums
        jogo.setGeneros(toGeneroList(dto.genres()));
        jogo.setPlataformas(toPlataformaList(dto.platforms()));
        jogo.setClassificacao(toClassificacaoIndicativa(dto.esrbRating()));

        String nomeDev = extrairNomeDesenvolvedorPrincipal(dto.developers());

        return new JogoMapeadoDto(jogo, nomeDev);
    }

    /*
        Método privado que extrai o nome do desenvolvedor
        da resposta da API.

        @param devDtos: Lista com os desenvolvedores do jogo.

        @return: Retorna uma única String com o nome do desenvolvedor
        principal do jogo. Em caso de falha devolve nulo.
    */
    private String extrairNomeDesenvolvedorPrincipal(List<RawgDeveloperDto> devDtos) {
        return Optional.ofNullable(devDtos)
                .filter(list -> !list.isEmpty()) // Fiotra a lista garantindo que não esteja vazia.
                .map(list -> list.get(0).name()) // Mapeia a lista para pegar o primeiro nome que aparece.
                .orElse(null);
    }

    /*
        Método privado que pega a lista de Generos recebida
        da API e a transforma em uma lista de ENUMS referente a
        nossa aplicação.

        @param generoDtos: Lista de Generos recebida da API.

        @return: Retorna uma lista transformada com todos os gêneros
        do jogo específico.
    */
    private List<Genero> toGeneroList(List<RawgGenreDto> generoDtos) {
        if (generoDtos == null) return Collections.emptyList(); // Em caso de lista vazia, retornar nulo.

        // Faz o mapeamento do objeto para transformar em itens do ENUM Genero.
        return generoDtos.stream().map(this::toGenero).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /*
        Método privado responsável por transformar uma lista
        de plataformas recebidas da API em objetos ENUM do tipo
        Plataforma.

        @param plataformaDtos: Lista de plataformas recebidas para realizar a
        conversão.

        @return: Retorna a lista convertida de Plataforma.
    */
    private List<Plataforma> toPlataformaList(List<RawgPlatformEntryDto> plataformaDtos) {
        if (plataformaDtos == null) return Collections.emptyList(); // Caso esteja vazia retorna nulo.

        // Faz o mapeamento do objeto e transforma no objeto Plataforma.
        return plataformaDtos.stream().map(entry -> toPlataforma(entry.platform())).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /*
        Pega maior parte das respostas esperadas da API
        e traduz para ENUMS da aplicação.

        Caso não encontre alguma define como 'OUTRO'.
    */
    private Genero toGenero(RawgGenreDto dto) {
        if (dto == null || dto.slug() == null) return null;
        return switch (dto.slug()) {
            case "action" -> Genero.ACAO;
            case "adventure" -> Genero.AVENTURA;
            case "role-playing-games-rpg" -> Genero.RPG;
            case "strategy" -> Genero.ESTRATEGIA;
            case "shooter" -> Genero.SHOOTER;
            case "simulation" -> Genero.SIMULACAO;
            case "puzzle" -> Genero.PUZZLE;
            case "platformer" -> Genero.PLATAFORMA;
            case "racing" -> Genero.CORRIDA;
            case "sports" -> Genero.ESPORTES;
            case "fighting" -> Genero.LUTA;
            case "horror" -> Genero.TERROR;
            case "survival" -> Genero.SURVIVAL;
            default -> {
                log.warn("Gênero não mapeado da API RAWG, categorizado como OUTROS: slug='{}', name='{}'", dto.slug(), dto.name());
                yield Genero.OUTROS;
            }
        };
    }

    /*
        Método que pega respostas esperadas de plataformas
        e traduz para o ENUM Plataforma.

        Caso não encontre cai no Enum 'OUTRO'.
    */
    private Plataforma toPlataforma(RawgPlatformDto dto) {
        if (dto == null || dto.slug() == null) return null;
        return switch (dto.slug()) {
            case "pc" -> Plataforma.PC;
            case "playstation5" -> Plataforma.PLAYSTATION_5;
            case "playstation4" -> Plataforma.PLAYSTATION_4;
            case "playstation3" -> Plataforma.PLAYSTATION_3;
            case "playstation2" -> Plataforma.PLAYSTATION_2;
            case "playstation1" -> Plataforma.PLAYSTATION_1;
            case "xbox-series-x" -> Plataforma.XBOX_SERIES;
            case "xbox-one" -> Plataforma.XBOX_ONE;
            case "xbox360" -> Plataforma.XBOX360;
            case "nintendo-switch" -> Plataforma.NINTENDO_SWITCH;
            case "nintendo-3ds" -> Plataforma.NINTENDO_3DS;
            case "nintendo-ds" -> Plataforma.NINTENDO_DS;
            case "macos" -> Plataforma.MACOS;
            case "linux" -> Plataforma.LINUX;
            case "android" -> Plataforma.ANDROID;
            case "ios" -> Plataforma.IOS;
            default -> {
                log.warn("Plataforma não mapeada da API RAWG, categorizada como OUTROS: slug='{}', name='{}'", dto.slug(), dto.name());
                yield Plataforma.OUTROS;
            }
        };
    }

    /*
        Método que traduz as classificações indicativas
        recebidas pela API e traduz para o objetos do Enum
        ClassificacaoIndicativa.
    */
    private ClassificacaoIndicativa toClassificacaoIndicativa(RawgEsrbRatingDto dto) {
        if (dto == null || dto.slug() == null) return null;
        return switch (dto.slug()) {
            case "everyone" -> ClassificacaoIndicativa.LIVRE;
            case "everyone-10-plus" -> ClassificacaoIndicativa.DEZ;
            case "teen" -> ClassificacaoIndicativa.DOZE;
            case "mature" -> ClassificacaoIndicativa.DEZESSEIS;
            case "adults-only" -> ClassificacaoIndicativa.DEZOITO;
            default -> null;
        };
    }
}
