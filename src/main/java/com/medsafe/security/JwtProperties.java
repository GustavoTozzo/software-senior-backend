package com.medsafe.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Configuracao dos tokens JWT (prefixo app.jwt). O segredo e usado para assinar com HS256,
 * que exige pelo menos 256 bits (32 bytes); em producao ele deve vir da variavel JWT_SECRET.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "app.jwt.secret precisa ter ao menos 32 caracteres (defina a variável de ambiente JWT_SECRET)");
        }
        if (expiration == null) {
            expiration = Duration.ofDays(7);
        }
    }
}
