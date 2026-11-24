package br.com.impactar.config;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação stateless via JWT (cookie httpOnly ou header Authorization).
 *
 * Usuário com troca de senha pendente recebe apenas ROLE_PASSWORD_CHANGE_REQUIRED:
 * só consegue chamar /auth/me, /auth/change-password e /auth/logout.
 *
 * CSRF desabilitado: cookie SameSite=Lax + API só aceita JSON
 * (formulário de outro site não consegue enviar application/json sem preflight).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String PASSWORD_CHANGE_REQUIRED = "PASSWORD_CHANGE_REQUIRED";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenResolver bearerTokenResolver) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/auth/change-password").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/instructor/**").hasAnyRole("ADMIN", "INSTRUCTOR")
                        .anyRequest().hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT"))
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(bearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::authoritiesFrom);
        return converter;
    }

    private static Collection<GrantedAuthority> authoritiesFrom(Jwt jwt) {
        if (Boolean.TRUE.equals(jwt.getClaimAsBoolean("mcp"))) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + PASSWORD_CHANGE_REQUIRED));
        }
        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
