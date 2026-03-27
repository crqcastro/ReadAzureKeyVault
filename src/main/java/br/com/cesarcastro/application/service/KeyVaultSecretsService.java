package br.com.cesarcastro.application.service;

import br.com.cesarcastro.application.port.out.KeyVaultSecretGateway;
import br.com.cesarcastro.domain.model.KeyVaultConnectionSettings;
import br.com.cesarcastro.domain.model.SecretEntry;

import java.util.List;
import java.util.Objects;

public class KeyVaultSecretsService {

    private final KeyVaultSecretGateway keyVaultSecretGateway;

    public KeyVaultSecretsService(KeyVaultSecretGateway keyVaultSecretGateway) {
        this.keyVaultSecretGateway = Objects.requireNonNull(keyVaultSecretGateway, "keyVaultSecretGateway é obrigatório");
    }

    public List<SecretEntry> loadSecrets(KeyVaultConnectionSettings settings) {
        Objects.requireNonNull(settings, "settings é obrigatório").validateRequiredFields();
        return keyVaultSecretGateway.listSecrets(settings);
    }

}

