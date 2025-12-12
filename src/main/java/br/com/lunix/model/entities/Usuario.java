package br.com.lunix.model.entities;

import br.com.lunix.model.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
    Entidade do banco de dados que receberá os usuários
    da aplicação.

    Implementa a interface **UserDetails** para indicar
    que esta é a classe que será utilizada para referenciar
    login e cadastro na aplicação.

    @Document - Define a classe como uma entidade no MongoDB.
    @Getter @Setter - Cria todos os getters e setters da classe.

    @NoArgsConstructor @AllArgsConstructor - Anotações que criam
    automaticamente os construtores pricipais da classe.

    @EqualsAndHashCode - Cria automaticamente métodos equals e hashcode
    para comparar a igualdade lógica entre objetos.
*/
@Document(collection = "usuarios")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Usuario implements UserDetails {

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String email;

    private String senha;

    private Set<Role> roles = new HashSet<>();

    @DBRef
    private Empresa empresa;

    private boolean ativo = true;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    // Método que define quais são as Roles que um usuário pode ter
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*
            Faz o stream dos cargos disponíveis e os mapeia a partir do nome
            para definir todos os cargos presentes na aplicação
        */

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    // Método que busca o usuário específico para login
    @Override
    public String getUsername() {
        return this.email;
    }

    // Método que busca a senah do usuário para realizar o login
    @Override
    public String getPassword() {
        return this.senha;
    }

    // Método que define se a conta está expirada ou não
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Método que define se a conta foi bloqueada ou não
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Método que indica se as credenciais do usuário expiraram
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Método que define se o usuário está habilitado
    @Override
    public boolean isEnabled() {
        return this.ativo;
    }
}
