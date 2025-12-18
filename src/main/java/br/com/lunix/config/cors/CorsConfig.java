package br.com.lunix.config.cors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${api.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Configura as origens permitidas
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // Configura os métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Configura os cabeçalhos permitidos
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));
        config.setExposedHeaders(List.of("x-auth-token"));

        // Permite o envio de credenciais
        config.setAllowCredentials(true);

        // Aplica a configuração nas rotas da API
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
