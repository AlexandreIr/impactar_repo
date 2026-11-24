package br.com.impactar.auth;

import java.util.UUID;

import br.com.impactar.user.Role;

import org.springframework.security.oauth2.jwt.Jwt;

/** Usuário autenticado da requisição (injetado direto nos controllers). */
public record CurrentUser(UUID id, Role role) {

    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(UUID.fromString(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString("role")));
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
