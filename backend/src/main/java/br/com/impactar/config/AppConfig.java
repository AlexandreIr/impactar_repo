package br.com.impactar.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @EnableAsync: envio de e-mail fora da requisição (usa o executor padrão do Spring Boot).
 * Clock: hora atual injetável — testes controlam a expiração dos links.
 */
@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
