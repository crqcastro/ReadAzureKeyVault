package br.com.cesarcastro.domain.model;

import java.time.Duration;
import java.util.Objects;

public record KeyVaultConnectionSettings(
        String uri,
        String clientId,
        String clientSecret,
        String tenantId,
        int maxRetries,
        Duration baseDelay,
        Duration maxDelay
) {

    public KeyVaultConnectionSettings {
        uri = normalize(uri);
        clientId = normalize(clientId);
        clientSecret = normalize(clientSecret);
        tenantId = normalize(tenantId);
        baseDelay = Objects.requireNonNull(baseDelay, "baseDelay é obrigatório");
        maxDelay = Objects.requireNonNull(maxDelay, "maxDelay é obrigatório");

        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries não pode ser negativo.");
        }
    }

    public static KeyVaultConnectionSettings fromUserInput(String uri, String clientId, String clientSecret, String tenantId) {
        return new KeyVaultConnectionSettings(uri, clientId, clientSecret, tenantId, 3, Duration.ofSeconds(1), Duration.ofSeconds(10));
    }

    public void validateRequiredFields() {
        if (uri.isBlank() || clientId.isBlank() || clientSecret.isBlank() || tenantId.isBlank()) {
            throw new IllegalArgumentException("Preencha todos os campos.");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

