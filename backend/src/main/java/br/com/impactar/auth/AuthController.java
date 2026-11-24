package br.com.impactar.auth;

import java.util.UUID;

import br.com.impactar.auth.dto.ChangePasswordRequest;
import br.com.impactar.auth.dto.LoginRequest;
import br.com.impactar.auth.dto.MeResponse;
import br.com.impactar.user.User;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/login            -> grava cookie + retorna usuário
 * POST /api/auth/logout           -> apaga cookie
 * GET  /api/auth/me               -> usuário da sessão
 * POST /api/auth/change-password  -> troca senha + novo cookie (mcp=false)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthCookieFactory cookieFactory;

    public AuthController(AuthService authService, JwtService jwtService, AuthCookieFactory cookieFactory) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieFactory = cookieFactory;
    }

    @PostMapping("/login")
    public ResponseEntity<MeResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        return withSession(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clear().toString())
                .build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return MeResponse.from(authService.getActiveUser(userId(jwt)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MeResponse> changePassword(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody ChangePasswordRequest request) {
        User user = authService.changePassword(userId(jwt), request.currentPassword(), request.newPassword());
        return withSession(user);
    }

    private ResponseEntity<MeResponse> withSession(User user) {
        String token = jwtService.issue(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.create(token).toString())
                .body(MeResponse.from(user));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
