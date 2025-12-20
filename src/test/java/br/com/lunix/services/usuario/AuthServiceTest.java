package br.com.lunix.services.usuario;

import br.com.lunix.dto.token.TokenResponseDto;
import br.com.lunix.dto.usuario.UsuarioLoginDto;
import br.com.lunix.dto.usuario.UsuarioProfileDto;
import br.com.lunix.dto.usuario.UsuarioRegistroDto;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.UsuarioMapper;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UsuarioMapper mapper;

    private Usuario usuario;
    private UsuarioRegistroDto registroDto;
    private UsuarioLoginDto loginDto;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setEmail("test@lunix.com");
        usuario.setSenha("encodedPass");
        usuario.setAtivo(true);

        registroDto = new UsuarioRegistroDto(
                "Test User", "test@lunix.com", "123456", "123456", null
        );

        loginDto = new UsuarioLoginDto("test@lunix.com", "123456");
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar tokens")
    void loginSucesso() {
        // Arrange
        Authentication authMock = mock(Authentication.class);
        when(authMock.getPrincipal()).thenReturn(usuario);

        // Simula a autenticação bem-sucedida pelo Spring Security
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);

        when(tokenService.generateAccessToken(usuario)).thenReturn("access-token");
        when(tokenService.generateRefreshToken(usuario)).thenReturn("refresh-token");

        // Act
        TokenResponseDto result = authService.login(loginDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("Deve lançar exceção se credenciais forem inválidas")
    void loginFalhaCredenciais() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void registrarSucesso() {
        // Arrange
        UsuarioProfileDto profileDto = new UsuarioProfileDto("user-1", "Test", "email", null);

        when(usuarioRepository.findByEmail(registroDto.email())).thenReturn(Optional.empty()); // Email livre
        when(mapper.toEntity(registroDto)).thenReturn(usuario);
        when(passwordEncoder.encode(registroDto.senha())).thenReturn("encodedPass");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(mapper.toProfileDto(usuario)).thenReturn(profileDto);

        // Act
        UsuarioProfileDto result = authService.registrar(registroDto);

        // Assert
        assertThat(result).isNotNull();
        verify(usuarioRepository).save(usuario);
        verify(passwordEncoder).encode(registroDto.senha());
    }

    @Test
    @DisplayName("Deve impedir registro se email já existe")
    void registrarEmailDuplicado() {
        when(usuarioRepository.findByEmail(registroDto.email())).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> authService.registrar(registroDto))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já está cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve vincular empresa se ID for informado")
    void registrarComEmpresa() {
        UsuarioRegistroDto dtoComEmpresa = new UsuarioRegistroDto(
                "Dev", "dev@lunix.com", "123", "123", "emp-1"
        );
        Empresa empresa = new Empresa();
        empresa.setId("emp-1");

        when(usuarioRepository.findByEmail(dtoComEmpresa.email())).thenReturn(Optional.empty());
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresa));
        when(mapper.toEntity(dtoComEmpresa)).thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        authService.registrar(dtoComEmpresa);

        assertThat(usuario.getEmpresa()).isEqualTo(empresa);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve falhar ao registrar com empresa inexistente")
    void registrarEmpresaInexistente() {
        UsuarioRegistroDto dtoComEmpresa = new UsuarioRegistroDto(
                "Dev", "dev@lunix.com", "123", "123", "emp-999"
        );

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(mapper.toEntity(dtoComEmpresa)).thenReturn(usuario);
        when(empresaRepository.findById("emp-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.registrar(dtoComEmpresa))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa não encontrada");
    }

    @Test
    @DisplayName("Deve renovar tokens com sucesso")
    void refreshTokenSucesso() {
        String tokenValido = "valid-refresh-token";

        when(tokenService.validateToken(tokenValido)).thenReturn("test@lunix.com");
        when(usuarioRepository.findByEmail("test@lunix.com")).thenReturn(Optional.of(usuario));
        when(tokenService.generateAccessToken(usuario)).thenReturn("new-access");
        when(tokenService.generateRefreshToken(usuario)).thenReturn("new-refresh");

        TokenResponseDto result = authService.refreshToken(tokenValido);

        assertThat(result.token()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");

        // Verifica se invalidou o antigo
        verify(tokenService).invalidateToken(tokenValido);
    }

    @Test
    @DisplayName("Deve falhar refresh se token for inválido")
    void refreshTokenInvalido() {
        when(tokenService.validateToken("invalid-token")).thenReturn("");

        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("inválido ou expirado");
    }

    @Test
    @DisplayName("Deve falhar refresh se usuário estiver desativado")
    void refreshTokenUsuarioDesativado() {
        usuario.setAtivo(false);
        String token = "token";

        when(tokenService.validateToken(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.refreshToken(token))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Conta desativada");
    }

    @Test
    @DisplayName("Deve extrair Bearer e invalidar token")
    void logoutSucesso() {
        String header = "Bearer meu-token-jwt";

        authService.logout(header);

        verify(tokenService).invalidateToken("meu-token-jwt");
    }
}