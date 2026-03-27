package br.com.cesarcastro.infrastructure.azure;

import br.com.cesarcastro.domain.model.KeyVaultConnectionSettings;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

public class AzureSecretClientFactory {

    public SecretClient create(KeyVaultConnectionSettings settings) {
        settings.validateRequiredFields();

        var credential = new ClientSecretCredentialBuilder()
                .clientId(settings.clientId())
                .clientSecret(settings.clientSecret())
                .tenantId(settings.tenantId())
                .build();

        var retryOptions = new RetryOptions(new ExponentialBackoffOptions()
                .setMaxRetries(settings.maxRetries())
                .setBaseDelay(settings.baseDelay())
                .setMaxDelay(settings.maxDelay()));

        return new SecretClientBuilder()
                .vaultUrl(settings.uri())
                .credential(credential)
                .retryOptions(retryOptions)
                .buildClient();
    }
}

