package br.com.cesarcastro.domain.model;

public record SecretEntry(String key, String value) {

    public SecretEntry {
        key = key == null ? "" : key;
        value = value == null ? "" : value;
    }

    public static SecretEntry fromVaultSecret(String secretName, String secretValue) {
        String envKey = secretName == null ? "" : secretName.toUpperCase().replace("-", "_");
        return new SecretEntry(envKey, secretValue);
    }

    public String asEnvLine() {
        return key + "=" + value;
    }
}

