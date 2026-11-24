package br.com.impactar.user.dto;

import br.com.impactar.user.Role;
import br.com.impactar.user.User;

import java.time.Instant;
import java.util.UUID;



public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        boolean active,
        boolean mustChangePassword,
        Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isMustChangePassword(),
                user.getCreatedAt());
    }
}
