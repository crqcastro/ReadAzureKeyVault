package br.com.cesarcastro.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryEntryTest {

    @Test
    void compactConstructor_treatsNullStringFieldsAsEmpty() {
        var entry = new HistoryEntry(null, null, null, null, List.of());

        assertEquals("", entry.timestamp());
        assertEquals("", entry.uri());
        assertEquals("", entry.clientId());
        assertEquals("", entry.tenantId());
    }

    @Test
    void compactConstructor_treatsNullSecretsAsEmptyList() {
        var entry = new HistoryEntry("ts", "uri", "cid", "tid", null);
        assertTrue(entry.secrets().isEmpty());
    }

    @Test
    void compactConstructor_secretsListIsImmutable() {
        var mutable = new ArrayList<SecretEntry>();
        mutable.add(new SecretEntry("K", "V"));
        var entry = new HistoryEntry("ts", "uri", "cid", "tid", mutable);

        mutable.add(new SecretEntry("K2", "V2"));
        assertEquals(1, entry.secrets().size());
        assertThrows(UnsupportedOperationException.class, () -> entry.secrets().add(new SecretEntry("K3", "V3")));
    }

    @Test
    void from_createsEntryFromSettingsAndSecrets() {
        var settings = KeyVaultConnectionSettings.fromUserInput("uri", "cid", "cs", "tid");
        var secrets = List.of(new SecretEntry("K", "V"));

        var entry = HistoryEntry.from("2026-01-01", settings, secrets);

        assertEquals("2026-01-01", entry.timestamp());
        assertEquals("uri", entry.uri());
        assertEquals("cid", entry.clientId());
        assertEquals("tid", entry.tenantId());
        assertEquals(secrets, entry.secrets());
    }

    @Test
    void displayLabel_returnsFormattedString() {
        var entry = new HistoryEntry("2026-01-01", "https://kv.vault.azure.net", "cid", "tid",
                List.of(new SecretEntry("K", "V"), new SecretEntry("K2", "V2")));

        assertEquals("2026-01-01  |  https://kv.vault.azure.net  (2 segredos)", entry.displayLabel());
    }

    @Test
    void toString_returnsDisplayLabel() {
        var entry = new HistoryEntry("ts", "uri", "cid", "tid", List.of());
        assertEquals(entry.displayLabel(), entry.toString());
    }
}
