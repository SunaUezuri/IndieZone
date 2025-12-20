package br.com.lunix.services.jogo;

import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.PrecoPlataforma;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.JogoRepository;
import br.com.lunix.services.itad.ItadApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JogoPrecoServiceTest {

    @InjectMocks
    private JogoPrecoService service;

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private ItadApiService itadApiService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private JogoSecurityService securityService;

    // Constante para simular o valor do application.properties
    private final String QUEUE_NAME = "indiezone.prices";

    @BeforeEach
    void setUp() {
        // Usando o reflection para injetar o valor na variável.
        ReflectionTestUtils.setField(service, "queueName", QUEUE_NAME);
    }

    @Test
    @DisplayName("Deve enviar ID do jogo para a fila do RabbitMQ")
    void enviarParaFilaSucesso() {
        String jogoId = "123";

        service.enviarParaFila(jogoId);

        // Verifica se o template foi chamado com o nome da fila e o payload corretos
        verify(rabbitTemplate).convertAndSend(QUEUE_NAME, jogoId);
    }

    @Test
    @DisplayName("Deve buscar todos os jogos e enviar cada um para a fila")
    void enviarTodosParaFilaSucesso() {
        // Cenário
        Jogo j1 = new Jogo(); j1.setId("1");
        Jogo j2 = new Jogo(); j2.setId("2");
        when(jogoRepository.findAll()).thenReturn(List.of(j1, j2));

        // Ação
        service.enviarTodosParaFila();

        // Verificação
        verify(rabbitTemplate, times(1)).convertAndSend(QUEUE_NAME, "1");
        verify(rabbitTemplate, times(1)).convertAndSend(QUEUE_NAME, "2");
    }

    @Test
    @DisplayName("solicitarAtualizacaoGlobalAdmin: Deve permitir e processar se for ADMIN")
    void solicitarAtualizacaoGlobalAdminSucesso() {
        Usuario admin = new Usuario();
        admin.setRoles(Set.of(Role.ROLE_ADMIN));

        Jogo jogoTeste = new Jogo();
        jogoTeste.setId("id-do-jogo-123");

        when(securityService.getUsuarioLogado()).thenReturn(admin);
        when(jogoRepository.findAll()).thenReturn(List.of(jogoTeste));

        service.solicitarAtualizacaoGlobalAdmin();

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq(QUEUE_NAME), eq("id-do-jogo-123"));
    }

    @Test
    @DisplayName("solicitarAtualizacaoGlobalAdmin: Deve bloquear se não for ADMIN")
    void solicitarAtualizacaoGlobalAdminBloqueio() {
        Usuario userComum = new Usuario();
        userComum.setRoles(Set.of(Role.ROLE_USER)); // Não é admin

        when(securityService.getUsuarioLogado()).thenReturn(userComum);

        assertThatThrownBy(() -> service.solicitarAtualizacaoGlobalAdmin())
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas admin");

        // Garante que não tentou buscar jogos nem enviar mensagens
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("processarAtualizacaoLogica: Deve atualizar preços quando API retorna dados")
    void processarAtualizacaoLogicaSucesso() {
        // Cenário
        String jogoId = "game-1";
        Jogo jogo = new Jogo();
        jogo.setId(jogoId);
        jogo.setTitulo("Hades");

        PrecoPlataforma preco = new PrecoPlataforma("Steam", 50.0, 100.0, 50, "url");
        List<PrecoPlataforma> novosPrecos = List.of(preco);

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(itadApiService.buscarPrecosParaJogo("Hades")).thenReturn(novosPrecos);

        // Ação
        boolean atualizou = service.processarAtualizacaoLogica(jogoId);

        // Verificação
        assertThat(atualizou).isTrue();
        assertThat(jogo.getPrecos()).hasSize(1);
        assertThat(jogo.getUltimaAtualizacaoPrecos()).isNotNull(); // Data foi atualizada
        verify(jogoRepository).save(jogo);
    }

    @Test
    @DisplayName("processarAtualizacaoLogica: Não deve atualizar se API retornar lista vazia")
    void processarAtualizacaoLogicaSemPrecos() {
        // Cenário
        String jogoId = "game-1";
        Jogo jogo = new Jogo();
        jogo.setId(jogoId);
        jogo.setTitulo("Jogo Desconhecido");

        when(jogoRepository.findById(jogoId)).thenReturn(Optional.of(jogo));
        when(itadApiService.buscarPrecosParaJogo("Jogo Desconhecido")).thenReturn(Collections.emptyList());

        // Ação
        boolean atualizou = service.processarAtualizacaoLogica(jogoId);

        // Verificação
        assertThat(atualizou).isFalse();
        verify(jogoRepository, never()).save(any()); // Não deve salvar se nada mudou
    }

    @Test
    @DisplayName("processarAtualizacaoLogica: Deve retornar false se jogo não existir no banco")
    void processarAtualizacaoLogicaJogoNaoEncontrado() {
        when(jogoRepository.findById("id-invalido")).thenReturn(Optional.empty());

        boolean atualizou = service.processarAtualizacaoLogica("id-invalido");

        assertThat(atualizou).isFalse();
        verifyNoInteractions(itadApiService); // Nem deve chamar a API
    }
}