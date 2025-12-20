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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;

    /*
        Realiza o login do usuário na aplicação e devolve um Token JWT
        @param dto - Entrada de dados para o login
    */
    public TokenResponseDto login(UsuarioLoginDto dto) {

        // Primeiro cria um token nativo do spring
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());

        // Manager chama o UserDetailsServiceImpl
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        Usuario usuario = (Usuario) auth.getPrincipal();

        // Gera os tokens JWT
        var accessToken = tokenService.generateAccessToken(usuario);
        var refreshToken = tokenService.generateRefreshToken(usuario);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    /*
        Registra usuários na aplicação com criptografiade senha
        @param dto - Dados de entrada do usuário para o registro
    */
    @Transactional
    public UsuarioProfileDto registrar(UsuarioRegistroDto dto) {

        // Validando se o email é único
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new RegraDeNegocioException("Este e-mail já está cadastrado.");
        }

        // Transformando o dto em entidade
        Usuario usuario = mapper.toEntity(dto);

        if (dto.idEmpresa() != null && !dto.idEmpresa().isEmpty()) {
            Empresa empresa = empresaRepository.findById(dto.idEmpresa())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o id"));

            usuario.setEmpresa(empresa);

        }

        // Criptografando a senha do usuário
        usuario.setSenha(passwordEncoder.encode(dto.senha()));

        usuario = usuarioRepository.save(usuario);

        return mapper.toProfileDto(usuario);
    }

    /*
        Método de refresh para gerar um novo token

        @param refreshToken - Token utilizado para recriar um novo
    */
    public TokenResponseDto refreshToken(String refreshToken) {
        String login = tokenService.validateToken(refreshToken);

        if (login.isEmpty()) {
            throw new RegraDeNegocioException("Refresh Token inválido ou expirado.");
        }

        Usuario usuario = usuarioRepository.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário do token não encontrado."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("Conta desativada. Não é possível renovar o token.");
        }

        String newAccessToken = tokenService.generateAccessToken(usuario);
        String newRefreshToken = tokenService.generateRefreshToken(usuario);

        tokenService.invalidateToken(refreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    /*
        Método para realizar logout e enviar o token
        para a blacklist no Redis

        @param token - Token a ser invalidado
    */
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        tokenService.invalidateToken(token);
    }
}
