package br.com.cesarcastro.application.service;

import br.com.cesarcastro.domain.model.KeyVaultConnectionSettings;
import br.com.cesarcastro.domain.model.SecretEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeyVaultSecretsServiceTest {

    private static final KeyVaultConnectionSettings VALID_SETTINGS =
            KeyVaultConnectionSettings.fromUserInput("https://kv.vault.azure.net", "cid", "cs", "tid");

    @Test
    void constructor_throwsOnNullGateway() {
        assertThrows(NullPointerException.class, () -> new KeyVaultSecretsService(null));
    }

    @Test
    void loadSecrets_throwsOnNullSettings() {
        var service = new KeyVaultSecretsService(settings -> List.of());
        assertThrows(NullPointerException.class, () -> service.loadSecrets(null));
    }

    @Test
    void loadSecrets_throwsWhenRequiredFieldsAreBlank() {
        var service = new KeyVaultSecretsService(settings -> List.of());
        var blankSettings = KeyVaultConnectionSettings.fromUserInput("", "cid", "cs", "tid");
        assertThrows(IllegalArgumentException.class, () -> service.loadSecrets(blankSettings));
    }

    @Test
    void loadSecrets_delegatesToGatewayAndReturnsResult() {
        var expected = List.of(new SecretEntry("API_KEY", "abc123"), new SecretEntry("DB_URL", "jdbc://localhost"));
        var service = new KeyVaultSecretsService(settings -> expected);

        var result = service.loadSecrets(VALID_SETTINGS);

        assertEquals(expected, result);
    }
}
