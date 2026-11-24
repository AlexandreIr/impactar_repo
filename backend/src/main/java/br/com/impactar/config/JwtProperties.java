package br.com.impactar.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        Duration expiration,
        String cookieName,
        boolean cookieSecure
) {
}
