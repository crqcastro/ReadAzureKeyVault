package br.com.cesarcastro.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class KeyVaultConnectionSettingsTest {

    @Test
    void fromUserInput_createsSettingsWithDefaultRetryConfig() {
        var settings = KeyVaultConnectionSettings.fromUserInput("uri", "cid", "cs", "tid");

        assertEquals("uri", settings.uri());
        assertEquals("cid", settings.clientId());
        assertEquals("cs", settings.clientSecret());
        assertEquals("tid", settings.tenantId());
        assertEquals(3, settings.maxRetries());
        assertEquals(Duration.ofSeconds(1), settings.baseDelay());
        assertEquals(Duration.ofSeconds(10), settings.maxDelay());
    }

    @Test
    void compactConstructor_trimsWhitespaceFromStringFields() {
        var settings = KeyVaultConnectionSettings.fromUserInput("  uri  ", " cid ", " cs ", " tid ");

        assertEquals("uri", settings.uri());
        assertEquals("cid", settings.clientId());
        assertEquals("cs", settings.clientSecret());
        assertEquals("tid", settings.tenantId());
    }

    @Test
    void compactConstructor_treatsNullFieldsAsEmptyString() {
        var settings = new KeyVaultConnectionSettings(null, null, null, null, 0, Duration.ofSeconds(1), Duration.ofSeconds(10));

        assertEquals("", settings.uri());
        assertEquals("", settings.clientId());
        assertEquals("", settings.clientSecret());
        assertEquals("", settings.tenantId());
    }

    @Test
    void compactConstructor_throwsNullPointerExceptionOnNullBaseDelay() {
        assertThrows(NullPointerException.class, () ->
                new KeyVaultConnectionSettings("u", "c", "s", "t", 0, null, Duration.ofSeconds(10)));
    }

    @Test
    void compactConstructor_throwsNullPointerExceptionOnNullMaxDelay() {
        assertThrows(NullPointerException.class, () ->
                new KeyVaultConnectionSettings("u", "c", "s", "t", 0, Duration.ofSeconds(1), null));
    }

    @Test
    void compactConstructor_throwsIllegalArgumentExceptionOnNegativeMaxRetries() {
        assertThrows(IllegalArgumentException.class, () ->
                new KeyVaultConnectionSettings("u", "c", "s", "t", -1, Duration.ofSeconds(1), Duration.ofSeconds(10)));
    }

    @Test
    void validateRequiredFields_throwsWhenUriIsBlank() {
        var settings = KeyVaultConnectionSettings.fromUserInput("", "cid", "cs", "tid");
        assertThrows(IllegalArgumentException.class, settings::validateRequiredFields);
    }

    @Test
    void validateRequiredFields_throwsWhenClientIdIsBlank() {
        var settings = KeyVaultConnectionSettings.fromUserInput("uri", "", "cs", "tid");
        assertThrows(IllegalArgumentException.class, settings::validateRequiredFields);
    }

    @Test
    void validateRequiredFields_throwsWhenClientSecretIsBlank() {
        var settings = KeyVaultConnectionSettings.fromUserInput("uri", "cid", "", "tid");
        assertThrows(IllegalArgumentException.class, settings::validateRequiredFields);
    }

    @Test
    void validateRequiredFields_throwsWhenTenantIdIsBlank() {
        var settings = KeyVaultConnectionSettings.fromUserInput("uri", "cid", "cs", "");
        assertThrows(IllegalArgumentException.class, settings::validateRequiredFields);
    }

    @Test
    void validateRequiredFields_passesWhenAllFieldsAreFilled() {
        var settings = KeyVaultConnectionSettings.fromUserInput("uri", "cid", "cs", "tid");
        assertDoesNotThrow(settings::validateRequiredFields);
    }
}
