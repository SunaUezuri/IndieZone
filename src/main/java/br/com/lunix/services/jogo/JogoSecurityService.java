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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/*
    Classe de serviço para gerenciar as permissões,
    autenticações e vínculos de dono.
*/
@Service
@RequiredArgsConstructor
public class JogoSecurityService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    /*
        Recupera o objeto Usuario completo do contexto de segurança (SecurityContextHolder).
    */
    public Usuario getUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        // Fallback: caso o principal seja apenas o email (String), busca no banco
        String email = (String) principal;
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado no banco."));
    }

    /*
        Valida se o usuário logado é dono do jogo (Autônomo ou Empresa) ou Admin.
        Lança RegraDeNegocioException se não tiver permissão.
    */
    public void validarPermissaoEdicao(Jogo jogo) {
        Usuario usuario = getUsuarioLogado();

        // Admin tem passe livre
        if (usuario.getRoles().contains(Role.ROLE_ADMIN)) return;

        // Verifica se é o Dev Autônomo dono do jogo
        boolean isDonoAutonomo = jogo.getDevAutonomo() != null &&
                jogo.getDevAutonomo().getId().equals(usuario.getId());

        // Verifica se a empresa do usuário é a mesma do jogo
        boolean isDonoEmpresa = jogo.getEmpresa() != null &&
                usuario.getEmpresa() != null &&
                jogo.getEmpresa().getId().equals(usuario.getEmpresa().getId());

        if (!isDonoAutonomo && !isDonoEmpresa) {
            throw new RegraDeNegocioException("Você não tem permissão para alterar este jogo.");
        }
    }

    // Método responsável por definir quem está cadastrando o jogo
    public void definirDonoDoJogo(Jogo jogo, JogoAdminRequestDto request) {
        Usuario usuario = getUsuarioLogado();

        if (usuario.getRoles().contains(Role.ROLE_ADMIN)) {
            vincularComoAdmin(jogo, request.empresaIdExistente(), request.devId());
        } else {
            vincularComoDev(jogo, usuario);
        }
    }

    // Lógica para quando um ADMIN cadastra um jogo: ele deve especificar quem é o dono.
    private void vincularComoAdmin(Jogo jogo, String empresaId, String devId){
        if (empresaId != null && !empresaId.isBlank()) {
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + empresaId));
            jogo.setEmpresa(empresa);

        } else if (devId != null && !devId.isBlank()) {
            Usuario dev = usuarioRepository.findById(devId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dev não encontrado: " + devId));
            jogo.setDevAutonomo(dev);

        } else {
            throw new RegraDeNegocioException("Admin deve informar ID da Empresa para vincular o jogo.");
        }
    }

    // Lógica para quando um DEV cadastra: vincula automaticamente ao seu perfil ou empresa.
    private void vincularComoDev(Jogo jogo, Usuario devLogado) {
        if (devLogado.getEmpresa() != null) {
            jogo.setEmpresa(devLogado.getEmpresa());
        } else {
            jogo.setDevAutonomo(devLogado);
        }
    }
}
