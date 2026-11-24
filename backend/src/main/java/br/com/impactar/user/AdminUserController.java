package br.com.impactar.user;

import java.util.UUID;

import br.com.impactar.shared.PageResponse;
import br.com.impactar.user.dto.CreateUserRequest;
import br.com.impactar.user.dto.UpdateUserRequest;
import br.com.impactar.user.dto.UserResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Somente ADMIN (regra /admin/** no SecurityConfig).
 *
 * GET  /api/admin/users?search=&page=&size=
 * POST /api/admin/users                      -> cria e envia credenciais
 * PUT  /api/admin/users/{id}                 -> nome, perfil, ativo
 * POST /api/admin/users/{id}/reset-password  -> nova senha provisória por e-mail
 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(name = "search", required = false) String search,
                                           @RequestParam(name = "page", defaultValue = "0") int page,
                                           @RequestParam(name = "size", defaultValue = "20") int size) {
        return userAdminService.list(search, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userAdminService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable("id") UUID id,
                               @Valid @RequestBody UpdateUserRequest request,
                               @AuthenticationPrincipal Jwt jwt) {
        return userAdminService.update(id, request, UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/{id}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable("id") UUID id) {
        userAdminService.resetPassword(id);
    }
}
