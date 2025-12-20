package br.com.lunix.services.jogo;

import br.com.lunix.dto.jogos.JogoAdminRequestDto;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Jogo;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.model.enums.Role;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JogoSecurityServiceTest {

    @InjectMocks
    private JogoSecurityService service;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    private Usuario admin;
    private Usuario devAutonomo;
    private Usuario devEmpresa;
    private Usuario usuarioComum;
    private Empresa empresa;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        // Configuração básica dos objetos
        empresa = new Empresa();
        empresa.setId("emp-1");
        empresa.setNome("Lunix Corp");

        admin = new Usuario();
        admin.setId("admin-1");
        admin.setEmail("admin@lunix.com");
        admin.setRoles(Set.of(Role.ROLE_ADMIN));

        devAutonomo = new Usuario();
        devAutonomo.setId("dev-1");
        devAutonomo.setEmail("dev@lunix.com");
        devAutonomo.setRoles(Set.of(Role.ROLE_DEV));

        devEmpresa = new Usuario();
        devEmpresa.setId("dev-2");
        devEmpresa.setRoles(Set.of(Role.ROLE_DEV));
        devEmpresa.setEmpresa(empresa);

        usuarioComum = new Usuario();
        usuarioComum.setId("user-1");
        usuarioComum.setRoles(Set.of(Role.ROLE_USER));

        jogo = new Jogo();
        jogo.setId("game-1");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getUsuarioLogado: Deve retornar usuário quando o Principal for um objeto Usuario")
    void getUsuarioLogadoObjetoCompleto() {
        mockarUsuarioLogado(admin);

        Usuario resultado = service.getUsuarioLogado();

        assertThat(resultado).isEqualTo(admin);
    }

    @Test
    @DisplayName("getUsuarioLogado: Deve buscar no banco quando o Principal for apenas Email (String)")
    void getUsuarioLogado_ApenasEmail() {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn("admin@lunix.com"); // Retorna String
        SecurityContextHolder.setContext(context);

        when(usuarioRepository.findByEmail("admin@lunix.com")).thenReturn(Optional.of(admin));

        Usuario resultado = service.getUsuarioLogado();

        assertThat(resultado).isEqualTo(admin);
    }

    @Test
    @DisplayName("getUsuarioLogado: Deve lançar erro se não encontrar usuário pelo email")
    void getUsuarioLogadoEmailNaoEncontrado() {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn("fantasma@lunix.com");
        SecurityContextHolder.setContext(context);

        when(usuarioRepository.findByEmail("fantasma@lunix.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUsuarioLogado())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("validarPermissaoEdicao: Admin deve ter permissão irrestrita")
    void validarPermissaoAdmin() {
        mockarUsuarioLogado(admin);

        service.validarPermissaoEdicao(jogo);
        // Se não lançou exceção, passou
    }

    @Test
    @DisplayName("validarPermissaoEdicao: Dev Autônomo deve editar seu próprio jogo")
    void validarPermissaoDonoAutonomo() {
        mockarUsuarioLogado(devAutonomo);
        jogo.setDevAutonomo(devAutonomo);

        service.validarPermissaoEdicao(jogo);
    }

    @Test
    @DisplayName("validarPermissaoEdicao: Dev de Empresa deve editar jogo da empresa")
    void validarPermissaoDonoEmpresa() {
        mockarUsuarioLogado(devEmpresa);
        jogo.setEmpresa(empresa); // Jogo da empresa emp-1

        service.validarPermissaoEdicao(jogo);
    }

    @Test
    @DisplayName("validarPermissaoEdicao: Deve bloquear terceiro tentando editar jogo alheio")
    void validarPermissaoSemPermissao() {
        mockarUsuarioLogado(usuarioComum);
        jogo.setDevAutonomo(devAutonomo); // Jogo é do dev-1

        assertThatThrownBy(() -> service.validarPermissaoEdicao(jogo))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não tem permissão");
    }

    @Test
    @DisplayName("definirDono: Dev Autônomo cadastra -> Jogo recebe devAutonomo")
    void definirDonoDevAutonomo() {
        mockarUsuarioLogado(devAutonomo);

        // Request null pois dev não precisa enviar IDs extras
        service.definirDonoDoJogo(jogo, null);

        assertThat(jogo.getDevAutonomo()).isEqualTo(devAutonomo);
        assertThat(jogo.getEmpresa()).isNull();
    }

    @Test
    @DisplayName("definirDono: Dev de Empresa cadastra -> Jogo recebe Empresa")
    void definirDonoDevEmpresa() {
        mockarUsuarioLogado(devEmpresa);

        service.definirDonoDoJogo(jogo, null);

        assertThat(jogo.getEmpresa()).isEqualTo(empresa);
        assertThat(jogo.getDevAutonomo()).isNull();
    }

    @Test
    @DisplayName("definirDono: Admin cadastra vinculando a uma Empresa existente")
    void definirDonoAdminParaEmpresa() {
        mockarUsuarioLogado(admin);
        JogoAdminRequestDto request = new JogoAdminRequestDto(null, null, "emp-1");

        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresa));

        service.definirDonoDoJogo(jogo, request);

        assertThat(jogo.getEmpresa()).isEqualTo(empresa);
    }

    @Test
    @DisplayName("definirDono: Admin cadastra vinculando a um Dev Autônomo existente")
    void definirDonoAdminParaDev() {
        mockarUsuarioLogado(admin);
        JogoAdminRequestDto request = new JogoAdminRequestDto(null, "dev-1", null);

        when(usuarioRepository.findById("dev-1")).thenReturn(Optional.of(devAutonomo));

        service.definirDonoDoJogo(jogo, request);

        assertThat(jogo.getDevAutonomo()).isEqualTo(devAutonomo);
    }

    @Test
    @DisplayName("definirDono: Admin tenta cadastrar sem informar nenhum dono (Erro)")
    void definirDonoAdminSemDono() {
        mockarUsuarioLogado(admin);
        JogoAdminRequestDto request = new JogoAdminRequestDto(null, null, null);

        assertThatThrownBy(() -> service.definirDonoDoJogo(jogo, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Admin deve informar ID");
    }

    @Test
    @DisplayName("definirDono: Admin informa ID de empresa inexistente")
    void definirDonoAdminEmpresaInexistente() {
        mockarUsuarioLogado(admin);
        JogoAdminRequestDto request = new JogoAdminRequestDto(null, null, "999");

        when(empresaRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.definirDonoDoJogo(jogo, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- MÉTODOS AUXILIARES ---

    private void mockarUsuarioLogado(Usuario usuario) {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(usuario);

        SecurityContextHolder.setContext(context);
    }
}