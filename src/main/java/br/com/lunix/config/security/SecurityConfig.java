package br.com.lunix.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
    Classe de configuração de seguraça da aplicação.

    @method PasswordEncoder - Método responsável por criptografar as senhas dos usuários.
    @method SecurityFilterChain - Método responsável por definir as rotas públicas e privadas da aplicação.
*/
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    /*
        Bean responsável por realizar a criptografia de senhas
        no momento do cadastro utilizando o BCryptPasswordEncoder.
    */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // Desliga CSRF (desnecessário para API REST)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Rotas Públicas (Login, Registro e Swagger)
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll() // Login e Register
                        .requestMatchers("/actuator/**").permitAll() // Monitoramento
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .requestMatchers("/jogos/import/**").hasAnyRole("DEV", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/jogos/meus-jogos").hasAnyRole("DEV", "ADMIN")

                        // Mudamos a controller, então a rota base agora é /dashboard
                        .requestMatchers("/admin/dashboard/**").hasRole("ADMIN")

                        // Rota de sync de preços (JogoController)
                        .requestMatchers(HttpMethod.POST, "/jogos/sync-prices").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/jogos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/empresas/**").permitAll()

                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll() // Swagger
                        // Todo o resto exige autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
