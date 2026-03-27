package br.com.cesarcastro.application.port.out;

import br.com.cesarcastro.domain.model.KeyVaultConnectionSettings;
import br.com.cesarcastro.domain.model.SecretEntry;

import java.util.List;

public interface KeyVaultSecretGateway {

    List<SecretEntry> listSecrets(KeyVaultConnectionSettings settings);
}

