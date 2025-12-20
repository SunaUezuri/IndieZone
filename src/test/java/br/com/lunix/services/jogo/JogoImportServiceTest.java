package br.com.lunix.services.jogo;

import br.com.lunix.dto.jogos.JogoMapeadoDto;
import br.com.lunix.dto.rawg.RawgRecords.RawgGameDto;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.RawgMapper;
import br.com.lunix.services.rawg.RawgApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JogoImportServiceTest {

    @InjectMocks
    private JogoImportService service;

    @Mock
    private RawgApiService rawgApiService;

    @Mock
    private RawgMapper rawgMapper;

    @Test
    @DisplayName("Deve importar dados com sucesso quando a API retorna resultados")
    void importarDadosRawgSucesso() {
        // CENÁRIO
        String titulo = "Hollow Knight";

        // Mock do objeto vindo da API
        RawgGameDto gameDtoMock = mock(RawgGameDto.class);

        // Mock do objeto final mapeado
        JogoMapeadoDto mapeadoDtoMock = mock(JogoMapeadoDto.class);

        // Configura o comportamento dos mocks
        // 1. A API retorna uma lista contendo o jogo
        when(rawgApiService.buscarJogos(titulo, 1)).thenReturn(List.of(gameDtoMock));

        // 2. O Mapper transforma esse jogo no DTO final
        when(rawgMapper.toJogoMapeado(gameDtoMock)).thenReturn(mapeadoDtoMock);

        // AÇÃO
        JogoMapeadoDto resultado = service.importarDadosRawg(titulo);

        // VERIFICAÇÃO
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEqualTo(mapeadoDtoMock);

        // Garante que o mapper foi chamado exatamente com o primeiro item da lista
        verify(rawgMapper).toJogoMapeado(gameDtoMock);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando a API retorna lista vazia")
    void importarDadosRawg_NaoEncontrado() {
        // CENÁRIO
        String titulo = "Jogo Inexistente 123";

        // A API retorna uma lista vazia
        when(rawgApiService.buscarJogos(titulo, 1)).thenReturn(Collections.emptyList());

        // AÇÃO E VERIFICAÇÃO
        assertThatThrownBy(() -> service.importarDadosRawg(titulo))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nenhum jogo encontrado");

        // Garante que o Mapper NUNCA foi chamado
        verifyNoInteractions(rawgMapper);
    }
}