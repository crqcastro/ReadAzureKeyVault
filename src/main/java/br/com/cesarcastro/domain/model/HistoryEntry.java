package br.com.cesarcastro.domain.model;

import java.util.List;

public record HistoryEntry(
        String timestamp,
        String uri,
        String clientId,
        String tenantId,
        List<SecretEntry> secrets
) {

    public HistoryEntry {
        timestamp = normalize(timestamp);
        uri = normalize(uri);
        clientId = normalize(clientId);
        tenantId = normalize(tenantId);
        secrets = secrets == null ? List.of() : List.copyOf(secrets);
    }

    public static HistoryEntry from(String timestamp, KeyVaultConnectionSettings settings, List<SecretEntry> secrets) {
        return new HistoryEntry(timestamp, settings.uri(), settings.clientId(), settings.tenantId(), secrets);
    }

    public String displayLabel() {
        return timestamp + "  |  " + uri + "  (" + secrets.size() + " segredos)";
    }

    @Override
    public String toString() {
        return displayLabel();
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
