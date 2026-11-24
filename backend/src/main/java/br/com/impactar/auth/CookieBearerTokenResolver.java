package br.com.impactar.auth;

import java.util.Set;

import br.com.impactar.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

/**
 * Lê o JWT do header Authorization (Bearer) ou, se ausente, do cookie httpOnly.
 *
 * Login e logout ignoram o token: um cookie expirado/inválido não pode
 * impedir o usuário de entrar novamente ou de limpar a sessão.
 */
@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final Set<String> IGNORED_PATHS = Set.of("/auth/login", "/auth/logout");

    private final DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();
    private final String cookieName;

    public CookieBearerTokenResolver(JwtProperties properties) {
        this.cookieName = properties.cookieName();
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (IGNORED_PATHS.contains(path)) {
            return null;
        }

        String fromHeader = headerResolver.resolve(request);
        if (fromHeader != null) {
            return fromHeader;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
