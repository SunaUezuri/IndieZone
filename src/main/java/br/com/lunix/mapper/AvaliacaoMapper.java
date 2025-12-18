package br.com.lunix.mapper;

import br.com.lunix.dto.avaliacao.AvaliacaoRequestDto;
import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.avaliacao.AvaliacaoUpdateDto;
import br.com.lunix.dto.usuario.UsuarioPublicProfileDto;
import br.com.lunix.model.entities.Avaliacao;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoMapper {

    private final UsuarioMapper usuarioMapper;

    public AvaliacaoMapper(UsuarioMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    /*
      Converte o DTO de requisição para a Entidade.
      OBS: Não setamos Usuário nem Jogo aqui, pois o RequestDto não traz esses IDs.
      Eles serão pegos do Token (Usuário) e da URL (Jogo) na Service.
    */
    public Avaliacao toEntity(AvaliacaoRequestDto dto) {
        if (dto == null) return null;

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        return avaliacao;
    }

    public void updateFromEntityDto(AvaliacaoUpdateDto dto, Avaliacao avaliacao) {
        if (dto == null || avaliacao == null) return;

        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());
    }

    /*
      Converte a Entidade para o DTO de resposta.
      Usa o UsuarioMapper para converter o autor da avaliação.
    */
    public AvaliacaoResponseDto toResponseDto(Avaliacao entity) {
        if (entity == null) return null;

        UsuarioPublicProfileDto usuarioDto = usuarioMapper.toPublicProfileDto(entity.getUsuario());

        return new AvaliacaoResponseDto(
                entity.getId(),
                entity.getNota(),
                entity.getComentario(),
                entity.getDataCriacao(),
                usuarioDto
        );
    }
}
