package br.com.lunix.services.jogo;

import br.com.lunix.dto.jogos.JogoMapeadoDto;
import br.com.lunix.dto.rawg.RawgRecords;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.RawgMapper;
import br.com.lunix.services.rawg.RawgApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/*
    Service responsável por importar dados da API do RAWG
    para facilitação no front-end.
*/
@Service
@RequiredArgsConstructor
public class JogoImportService {

    private final RawgApiService rawgApiService;
    private final RawgMapper rawgMapper;

    /*
        Método que pega o título do jogo e devolve
        dados encontrados na API

        @param titulo - Título do jogo a ser importado
    */
    public JogoMapeadoDto importarDadosRawg(String titulo) {
        List<RawgRecords.RawgGameDto> resultados = rawgApiService.buscarJogos(titulo, 1);
        if (resultados.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum jogo encontrado na RAWG com o título: " + titulo);
        }
        return rawgMapper.toJogoMapeado(resultados.get(0));
    }
}
