package br.com.impactar.auth;

import br.com.impactar.config.JwtProperties;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

    private final JwtProperties properties;

    public AuthCookieFactory(JwtProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie create(String token) {
        return base(token).maxAge(properties.expiration()).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(properties.cookieName(), value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Lax")
                .path("/");
    }
}
