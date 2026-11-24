package br.com.impactar.auth.dto;

import java.util.UUID;

import br.com.impactar.user.Role;
import br.com.impactar.user.User;

public record MeResponse(
        UUID id,
        String name,
        String email,
        Role role,
        boolean mustChangePassword
) {

    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isMustChangePassword());
    }
}
