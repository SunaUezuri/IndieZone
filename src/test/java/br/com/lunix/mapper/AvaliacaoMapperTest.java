package br.com.lunix.mapper;

import br.com.lunix.dto.avaliacao.AvaliacaoRequestDto;
import br.com.lunix.dto.avaliacao.AvaliacaoResponseDto;
import br.com.lunix.dto.avaliacao.AvaliacaoUpdateDto;
import br.com.lunix.dto.usuario.UsuarioPublicProfileDto;
import br.com.lunix.model.entities.Avaliacao;
import br.com.lunix.model.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoMapperTest {

    // Cria o mock
    @Mock
    private UsuarioMapper usuarioMapper;

    // Injeta o mock dentro da classe
    @InjectMocks
    private AvaliacaoMapper mapper;

    private Avaliacao avaliacaoBase;
    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioBase = new Usuario();
        usuarioBase.setId("user-1");
        usuarioBase.setNome("João Tester");

        avaliacaoBase = new Avaliacao();
        avaliacaoBase.setId("av-1");
        avaliacaoBase.setNota(9.5);
        avaliacaoBase.setComentario("Jogo excelente!");
        avaliacaoBase.setUsuario(usuarioBase);
        avaliacaoBase.setDataCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve converter RequestDto para Entidade corretamente")
    void toEntitySucesso() {
        // Cenário
        AvaliacaoRequestDto dto = new AvaliacaoRequestDto(8.0, "Bom jogo");

        // Ação
        Avaliacao resultado = mapper.toEntity(dto);

        // Verificação
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNota()).isEqualTo(8.0);
        assertThat(resultado.getComentario()).isEqualTo("Bom jogo");
        // Garante que não tentou setar usuário ou jogo neste momento
        assertThat(resultado.getUsuario()).isNull();
        assertThat(resultado.getJogo()).isNull();
    }

    @Test
    @DisplayName("toEntity deve retornar null se o DTO for nulo")
    void toEntityNulo() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Deve atualizar entidade existente com dados do UpdateDto")
    void updateFromEntityDtoSucesso() {
        // Cenário
        AvaliacaoUpdateDto dto = new AvaliacaoUpdateDto(10, "Editado: Agora é 10/10");

        // Ação
        mapper.updateFromEntityDto(dto, avaliacaoBase);

        // Verificação
        assertThat(avaliacaoBase.getNota()).isEqualTo(10.0); // O DTO passa Integer, mas a entidade é Double
        assertThat(avaliacaoBase.getComentario()).isEqualTo("Editado: Agora é 10/10");
    }

    @Test
    @DisplayName("updateFromEntityDto não deve fazer nada se DTO ou Entidade forem nulos")
    void updateFromEntityDtoNulo() {
        // Testa DTO nulo
        mapper.updateFromEntityDto(null, avaliacaoBase);
        assertThat(avaliacaoBase.getNota()).isEqualTo(9.5); // Mantém original

        // Testa Entidade nula
        AvaliacaoUpdateDto dto = new AvaliacaoUpdateDto(5, "Teste");
        mapper.updateFromEntityDto(dto, null);
        // Se não lançou NullPointerException, passou
    }

    @Test
    @DisplayName("Deve converter Entidade para ResponseDto e mapear o usuário usando o mock")
    void toResponseDtoSucesso() {
        // Cenário
        // Ensinando o Mockito sobre o que ele deve responder
        UsuarioPublicProfileDto usuarioDtoMock = new UsuarioPublicProfileDto("user-1", "João Tester");

        when(usuarioMapper.toPublicProfileDto(any(Usuario.class)))
                .thenReturn(usuarioDtoMock);

        // Ação
        AvaliacaoResponseDto resultado = mapper.toResponseDto(avaliacaoBase);

        // Verificação
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(avaliacaoBase.getId());
        assertThat(resultado.nota()).isEqualTo(avaliacaoBase.getNota());
        assertThat(resultado.comentario()).isEqualTo(avaliacaoBase.getComentario());

        // Verifica se o usuário aninhado veio corretamente do mock
        assertThat(resultado.usuario()).isNotNull();
        assertThat(resultado.usuario().nome()).isEqualTo("João Tester");

        // Verifica se o método do UsuarioMapper foi realmente chamado 1 vez
        verify(usuarioMapper).toPublicProfileDto(usuarioBase);
    }

    @Test
    @DisplayName("toResponseDto deve retornar null se entidade for nula")
    void toResponseDtoNulo() {
        assertThat(mapper.toResponseDto(null)).isNull();
    }
}