package br.com.cesarcastro.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecretEntryTest {

    @Test
    void compactConstructor_treatsNullKeyAsEmptyString() {
        var entry = new SecretEntry(null, "value");
        assertEquals("", entry.key());
    }

    @Test
    void compactConstructor_treatsNullValueAsEmptyString() {
        var entry = new SecretEntry("KEY", null);
        assertEquals("", entry.value());
    }

    @Test
    void fromVaultSecret_convertsNameToUppercaseAndReplacesHyphens() {
        var entry = SecretEntry.fromVaultSecret("my-secret-key", "secret-value");
        assertEquals("MY_SECRET_KEY", entry.key());
        assertEquals("secret-value", entry.value());
    }

    @Test
    void fromVaultSecret_treatsNullNameAsEmptyKey() {
        var entry = SecretEntry.fromVaultSecret(null, "value");
        assertEquals("", entry.key());
        assertEquals("value", entry.value());
    }

    @Test
    void asEnvLine_returnsKeyEqualsValue() {
        var entry = new SecretEntry("API_KEY", "abc123");
        assertEquals("API_KEY=abc123", entry.asEnvLine());
    }
}
