package br.com.lunix.services.empresa;

import br.com.lunix.dto.empresa.EmpresaDetalhesDto;
import br.com.lunix.dto.empresa.EmpresaRequestDto;
import br.com.lunix.dto.empresa.EmpresaResponseDto;
import br.com.lunix.dto.empresa.EmpresaUpdateDto;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.mapper.EmpresaMapper;
import br.com.lunix.mapper.JogoMapper;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.repository.UsuarioRepository;
import br.com.lunix.services.igdb.IgdbApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @InjectMocks
    private EmpresaService service;

    @Mock private EmpresaRepository repository;
    @Mock private JogoRepository jogoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IgdbApiService igdbApiService;
    @Mock private JogoMapper jogoMapper;
    @Mock private EmpresaMapper mapper;

    private Empresa empresa;
    private EmpresaRequestDto requestDto;
    private EmpresaResponseDto responseDto;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId("emp-1");
        empresa.setNome("Lunix Corp");
        empresa.setUrlLogo("http://logo.com");

        requestDto = new EmpresaRequestDto("Lunix Corp", "Desc", "BR", "http://logo.com");
        responseDto = new EmpresaResponseDto("emp-1", "Lunix Corp", "BR", "http://logo.com");
    }

    @Test
    @DisplayName("Deve criar empresa com sucesso quando nome é único")
    void createSucesso() {
        when(repository.findByNomeIgnoreCase("Lunix Corp")).thenReturn(Optional.empty());
        when(mapper.toEntity(requestDto)).thenReturn(empresa);
        when(repository.save(empresa)).thenReturn(empresa);
        when(mapper.toResponseDto(empresa)).thenReturn(responseDto);

        EmpresaResponseDto result = service.create(requestDto);

        assertThat(result).isNotNull();
        verify(repository).save(empresa);
        verifyNoInteractions(igdbApiService);
    }

    @Test
    @DisplayName("Deve buscar logo automaticamente se a URL vier vazia")
    void createBuscaLogoAutomatica() {
        EmpresaRequestDto dtoSemLogo = new EmpresaRequestDto("Valve", "Desc", "USA", "");
        Empresa empresaSemLogo = new Empresa();
        empresaSemLogo.setNome("Valve");

        when(repository.findByNomeIgnoreCase("Valve")).thenReturn(Optional.empty());
        when(mapper.toEntity(dtoSemLogo)).thenReturn(empresaSemLogo);
        when(igdbApiService.buscarLogoEmpresa("Valve")).thenReturn("http://igdb.com/valve.png");
        when(repository.save(empresaSemLogo)).thenReturn(empresaSemLogo);

        service.create(dtoSemLogo);

        assertThat(empresaSemLogo.getUrlLogo()).isEqualTo("http://igdb.com/valve.png");
        verify(igdbApiService).buscarLogoEmpresa("Valve");
    }

    @Test
    @DisplayName("Deve lançar exceção se nome da empresa já existir")
    void createNomeDuplicado() {
        when(repository.findByNomeIgnoreCase(requestDto.nome())).thenReturn(Optional.of(empresa));

        assertThatThrownBy(() -> service.create(requestDto))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Já existe uma empresa");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar empresa com sucesso")
    void updateSucesso() {
        EmpresaUpdateDto updateDto = new EmpresaUpdateDto("Lunix Corp", "Nova Desc", "BR", "url");

        when(repository.findById("emp-1")).thenReturn(Optional.of(empresa));
        when(repository.save(empresa)).thenReturn(empresa);
        when(mapper.toResponseDto(empresa)).thenReturn(responseDto);

        service.update("emp-1", updateDto);

        verify(mapper).updateEntityFromDto(updateDto, empresa);
        verify(repository).save(empresa);
    }

    @Test
    @DisplayName("Deve impedir atualização se o novo nome pertencer a outra empresa")
    void updateNomeDuplicado() {
        EmpresaUpdateDto updateDto = new EmpresaUpdateDto("Outra Corp", "Desc", "BR", "url");

        when(repository.findById("emp-1")).thenReturn(Optional.of(empresa));

        when(repository.findByNomeIgnoreCase("Outra Corp")).thenReturn(Optional.of(new Empresa()));

        assertThatThrownBy(() -> service.update("emp-1", updateDto))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Já existe uma empresa");
    }

    @Test
    @DisplayName("Deve deletar empresa se não houver vínculos")
    void deleteSucesso() {
        when(repository.findById("emp-1")).thenReturn(Optional.of(empresa));
        when(jogoRepository.existsByEmpresa(empresa)).thenReturn(false);
        when(usuarioRepository.existsByEmpresa(empresa)).thenReturn(false);

        service.delete("emp-1");

        verify(repository).delete(empresa);
    }

    @Test
    @DisplayName("Deve impedir deleção se houver jogos vinculados")
    void deleteComJogosVinculados() {
        when(repository.findById("emp-1")).thenReturn(Optional.of(empresa));
        when(jogoRepository.existsByEmpresa(empresa)).thenReturn(true);

        assertThatThrownBy(() -> service.delete("emp-1"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("existem JOGOS vinculados");

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve impedir deleção se houver usuários vinculados")
    void deleteComUsuariosVinculados() {
        when(repository.findById("emp-1")).thenReturn(Optional.of(empresa));
        when(jogoRepository.existsByEmpresa(empresa)).thenReturn(false);
        when(usuarioRepository.existsByEmpresa(empresa)).thenReturn(true);

        assertThatThrownBy(() -> service.delete("emp-1"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("existem USUÁRIOS vinculados");
    }

    @Test
    @DisplayName("Deve buscar detalhes da empresa e listar seus jogos")
    void findByIdSucesso() {
        when(repository.findById("emp-1")).thenReturn(Optional.of(empresa));

        when(jogoRepository.findByEmpresa(any(Empresa.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        EmpresaDetalhesDto detalhesDto = mock(EmpresaDetalhesDto.class);
        when(mapper.toDetalhesDto(eq(empresa), anyList())).thenReturn(detalhesDto);

        EmpresaDetalhesDto result = service.findById("emp-1");

        assertThat(result).isNotNull();

        verify(jogoRepository).findByEmpresa(eq(empresa), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve listar todas as empresas paginadas")
    void findAllSucesso() {
        Page<Empresa> page = new PageImpl<>(List.of(empresa));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<EmpresaResponseDto> result = service.findAll(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar por país paginado")
    void findByPaisSucesso() {
        Page<Empresa> page = new PageImpl<>(List.of(empresa));
        when(repository.findByPaisOrigemIgnoreCase(eq("BR"), any(Pageable.class))).thenReturn(page);

        service.findByPais("BR", 0, 10);

        verify(repository).findByPaisOrigemIgnoreCase(eq("BR"), any(Pageable.class));
    }
}