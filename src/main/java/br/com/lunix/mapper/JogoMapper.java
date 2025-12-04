package br.com.lunix.mapper;

import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.jogos.*;
import br.com.lunix.model.entities.Jogo;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class JogoMapper {

    /*
      Converte o DTO de cadastro básico para a Entidade.
      OBS: Empresa e DevAutonomo são definidos na Service (via Token ou Admin DTO).
    */
    public Jogo toEntity(JogoRequestDto dto) {
        if (dto == null) return null;

        Jogo jogo = new Jogo();
        jogo.setTitulo(dto.titulo());
        jogo.setDescricao(dto.descricao());
        jogo.setUrlCapa(dto.urlCapa());
        jogo.setDataLancamento(dto.dataLancamento());
        jogo.setClassificacao(dto.classificacao());
        jogo.setGeneros(dto.generos());
        jogo.setPlataformas(dto.plataformas());

        // Valores padrão de inicialização
        jogo.setNotaMedia(0.0);
        jogo.setTotalAvaliacoes(0);
        jogo.setPrecos(Collections.emptyList());

        return jogo;
    }

    /*
      Atualiza dados de um jogo existente.
    */
    public void updateEntityFromDto(JogoUpdateDto dto, Jogo jogo) {
        if (dto == null || jogo == null) return;

        jogo.setTitulo(dto.titulo());
        jogo.setDescricao(dto.descricao());
        jogo.setUrlCapa(dto.urlCapa());
        jogo.setDataLancamento(dto.dataLancamento());
        jogo.setClassificacao(dto.classificacao());

        if (dto.generos() != null && !dto.generos().isEmpty()) {
            jogo.setGeneros(dto.generos());
        }

        if (dto.plataformas() != null && !dto.plataformas().isEmpty()) {
            jogo.setPlataformas(dto.plataformas());
        }
    }

    /*
      Atualiza apenas os gêneros (PATCH).
    */
    public void updateGeneros(JogoGenresPatchDto dto, Jogo jogo) {
        if (dto != null && jogo != null && dto.generos() != null) {
            jogo.setGeneros(dto.generos());
        }
    }

    /*
      DTO leve para listagens (Home Page).
    */
    public JogoResponseDto toResponseDto(Jogo jogo) {
        if (jogo == null) return null;

        return new JogoResponseDto(
                jogo.getId(),
                jogo.getTitulo(),
                jogo.getUrlCapa(),
                determinarNomeCriador(jogo),
                jogo.getNotaMedia(),
                jogo.getClassificacao()
        );
    }

    /*
      DTO detalhado para a página do jogo.
      A Service deve buscá-las no AvaliacaoRepository, convertê-las e passá-las para este método.
    */
    public JogoDetalhesDto toDetalhesDto(
            Jogo jogo,
            List<AvaliacaoResponseDto> avAdmin,
            List<AvaliacaoResponseDto> avDev,
            List<AvaliacaoResponseDto> avUser
    ) {
        if (jogo == null) return null;

        return new JogoDetalhesDto(
                jogo.getId(),
                jogo.getTitulo(),
                jogo.getDescricao(),
                jogo.getUrlCapa(),
                jogo.getDataLancamento(),
                jogo.getClassificacao(),
                jogo.getGeneros(),
                jogo.getPlataformas(),
                jogo.getNotaMedia(),
                (int) jogo.getTotalAvaliacoes(),
                determinarNomeCriador(jogo),
                avAdmin != null ? avAdmin : Collections.emptyList(),
                avUser != null ? avUser : Collections.emptyList(),
                avDev != null ? avDev : Collections.emptyList()
        );
    }

    /*
      Define quem deve aparecer como "Criador": A Empresa ou o Dev Autônomo.
    */
    private String determinarNomeCriador(Jogo jogo) {
        if (jogo.getEmpresa() != null) {
            return jogo.getEmpresa().getNome();
        } else if (jogo.getDevAutonomo() != null) {
            return jogo.getDevAutonomo().getNome();
        }
        return "Desconhecido";
    }
}
