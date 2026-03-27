package br.com.cesarcastro.bootstrap;

import br.com.cesarcastro.application.service.KeyVaultSecretsService;
import br.com.cesarcastro.infrastructure.azure.AzureKeyVaultSecretGateway;
import br.com.cesarcastro.infrastructure.azure.AzureSecretClientFactory;
import br.com.cesarcastro.infrastructure.persistence.FileHistoryRepository;
import br.com.cesarcastro.presentation.ui.KeyVaultGui;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        var keyVaultSecretsService = new KeyVaultSecretsService(
                new AzureKeyVaultSecretGateway(new AzureSecretClientFactory())
        );
        var historyRepository = new FileHistoryRepository(
                Path.of(System.getProperty("user.home"), "keyvault-history.txt")
        );

        KeyVaultGui.launch(keyVaultSecretsService, historyRepository);
    }
}
