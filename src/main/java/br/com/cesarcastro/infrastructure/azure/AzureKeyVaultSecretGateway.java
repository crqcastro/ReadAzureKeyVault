package br.com.cesarcastro.infrastructure.azure;

import br.com.cesarcastro.application.port.out.KeyVaultSecretGateway;
import br.com.cesarcastro.domain.model.KeyVaultConnectionSettings;
import br.com.cesarcastro.domain.model.SecretEntry;
import com.azure.security.keyvault.secrets.SecretClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AzureKeyVaultSecretGateway implements KeyVaultSecretGateway {

    private final AzureSecretClientFactory secretClientFactory;

    public AzureKeyVaultSecretGateway(AzureSecretClientFactory secretClientFactory) {
        this.secretClientFactory = Objects.requireNonNull(secretClientFactory, "secretClientFactory é obrigatório");
    }

    @Override
    public List<SecretEntry> listSecrets(KeyVaultConnectionSettings settings) {
        SecretClient secretClient = secretClientFactory.create(settings);
        List<SecretEntry> secrets = new ArrayList<>();

        // Limitação conhecida: o Azure SDK não suporta busca em batch de valores de segredos.
        // Cada chamada a getSecret() resulta em uma requisição HTTP individual (problema N+1).
        secretClient.listPropertiesOfSecrets().forEach(properties -> {
            String secretName = properties.getName();
            String secretValue = secretClient.getSecret(secretName).getValue();
            secrets.add(SecretEntry.fromVaultSecret(secretName, secretValue));
        });

        return secrets;
    }

}

