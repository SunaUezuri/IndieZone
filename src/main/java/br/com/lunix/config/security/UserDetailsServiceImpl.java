package br.com.lunix.config.security;

import br.com.lunix.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
    Serviço responsável por controlar o acesso dos usuários
    e controlar o login dos mesmos.

    Implementa a interface UserDetailsService para acesso a métodos
    que permitem o controle de entrada dos usuários.
*/
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // Repositório injetado de usuário para acesso ao documento de usuários
    @Autowired
    private UsuarioRepository repository;

    /*
        Método que procura um usuário utilizando o parâmetro escolhido.

        @param email - email utilizado para buscar o usuário.

        Em caso de erro lança uma exceção de usuário não encontrado.

        return: Retorna o usuário com email inserido.
    */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
    }
}
