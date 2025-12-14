package br.com.lunix.services;

import br.com.lunix.dto.usuario.*;
import br.com.lunix.exceptions.RegraDeNegocioException;
import br.com.lunix.exceptions.ResourceNotFoundException;
import br.com.lunix.mapper.UsuarioMapper;
import br.com.lunix.model.entities.Empresa;
import br.com.lunix.model.entities.Usuario;
import br.com.lunix.repository.EmpresaRepository;
import br.com.lunix.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final EmpresaRepository empresaRepository;

    private final UsuarioMapper mapper;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /*
        Realiza o login do usuário na aplicação e devolve um Token JWT
        @param dto - Entrada de dados para o login
    */
    public TokenResponseDto login(UsuarioLoginDto dto) {

        // Primeiro cria um token nativo do spring
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());

        // Manager chama o UserDetailsServiceImpl
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        // Gera o token JWT
        var token = tokenService.generateToken((Usuario) auth.getPrincipal());

        return new TokenResponseDto(token);
    }

    /*
        Registra usuários na aplicação com criptografiade senha
        @param dto - Dados de entrada do usuário para o registro
    */
    @Transactional
    public UsuarioProfileDto registrar(UsuarioRegistroDto dto) {

        // Validando se o email é único
        if (repository.findByEmail(dto.email()).isPresent()) {
            throw new RegraDeNegocioException("Este e-mail já está cadastrado.");
        }

        // Transformando o dto em entidade
        Usuario usuario = mapper.toEntity(dto);

        if (!dto.idEmpresa().isEmpty()) {
            Empresa empresa = empresaRepository.findById(dto.idEmpresa())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com o id"));

            usuario.setEmpresa(empresa);

        }

        // Criptografando a senha do usuário
        usuario.setSenha(passwordEncoder.encode(dto.senha()));

        usuario = repository.save(usuario);

        return mapper.toProfileDto(usuario);
    }

    /*
        Lista todos os usuários da aplicação de forma paginada
        @param page - Quantidade de páginas a serem devolvidas
        @param size - Tamanho das páginas
    */
    @Transactional(readOnly = true)
    public Page<UsuarioAdminListDto> listarTodos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<Usuario> usuarios = repository.findAll(pageable);

        return usuarios.map(mapper::toAdminListDto);
    }

    /*
        Busca usuários por pelo nome parcial com paginação
        @param nome - Nome a ser pesquisados
        @param page - Quantidade de páginas a serem devolvidas
        @param size - Tamanho das páginas
    */
    @Transactional(readOnly = true)
    public Page<UsuarioAdminListDto> buscarPorNome(String nome, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());

        Page<Usuario> usuarios = repository.findByNomeContainingIgnoreCase(nome, pageable);

        return usuarios.map(mapper::toAdminListDto);
    }

    /*
        Busca um usuário pelo id do mesmo
        @param id - ID do usuário a ser buscado
    */
    @Transactional(readOnly = true)
    public UsuarioProfileDto buscarPorId(String id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + id));

        return mapper.toProfileDto(usuario);
    }

    /*
        Atualiza os dados de um usuário
        @param id - ID do usuário a ser atualizado
        @param dto - Novos dados para atualizar
    */
    @Transactional
    public UsuarioAdminListDto atualizar(String id,UsuarioUpdateDto dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + id));

        mapper.updateEntityFromDto(dto, usuario);

        // Lógica manual para vincular Empresa (se o ID for passado)
        if (dto.idEmpresa() != null && !dto.idEmpresa().isBlank()) {
            Empresa empresa = empresaRepository.findById(dto.idEmpresa())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. ID: " + dto.idEmpresa()));
            usuario.setEmpresa(empresa);
        } else {
            if (dto.idEmpresa() != null && dto.idEmpresa().isEmpty()) {
                usuario.setEmpresa(null);
            }
        }

        usuario = repository.save(usuario);
        return mapper.toAdminListDto(usuario);
    }

    /*
        Atualiza apenas as roles de um usuário
        @param id - ID do usuário a ter as roles atualizadas
        @param dto - Roles a serem atualizadas
    */
    @Transactional
    public UsuarioAdminListDto atualizarRoles(String id, UsuarioRolePatchDto dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + id));

        usuario.setRoles(dto.roles());

        usuario = repository.save(usuario);

        return mapper.toAdminListDto(usuario);
    }

    /*
        Realiza uma exclusão lógica
        Usuário não é removido do banco, apenas desativado

        @param id - ID do usuário a ser desativado
    */
    @Transactional
    public void desativar(String id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + id));

        usuario.setAtivo(false);
        repository.save(usuario);
    }
}
