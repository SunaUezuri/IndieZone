package br.com.lunix.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
    Classe de configuração de seguraça da aplicação.

    @method PasswordEncoder - Método responsável por criptografar as senhas dos usuários.
    @method SecurityFilterChain - Método responsável por definir as rotas públicas e privadas da aplicação.
*/
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /*
        Bean responsável por realizar a criptografia de senhas
        no momento do cadastro utilizando o BCryptPasswordEncoder.
    */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
        Bean que define as rotas públicas e privadas da aplicação
        indicando também quais rtas cada tipo de usuário pode acessar
        e que métodos realizar.

        User - Só tem permissões de leitura e acesso a páginas públicas como
        a de login, registro, home e etc.

        Dev - Permissão de leitura assim como Users e permissões de escrita, edição e
        delete de jogos próprios do desenvolvedor, além de acesso a páginas exclusivas de
        Devs para dashboards sobre seus jogos.

        Admin - Acesso total a todas as funcionalidades da aplicação e exclusivos como assuntos relacionados
        as empresas, controle de usuários e dashboards informativos.
    */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Desabilita os recursos abstratos HTTP
                .authorizeHttpRequests(authz -> authz
                        // Rotas públicas
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/jogos", "/jogos/**", "/empresas", "/empresas/**", "/sobre").permitAll()
                        .requestMatchers("/login", "/registro/**").permitAll()

                        // Permissões de Dev
                        .requestMatchers("/dev/**").hasRole("DEV")

                        // Permissões de ADM
                        .requestMatchers("/admin").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

                return http.build();
    }
}
