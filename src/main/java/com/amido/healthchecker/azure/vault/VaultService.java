package com.amido.healthchecker.azure.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VaultService {

    @Value("${vault.base.url}")
    private String vaultBaseUrl;

    @Value("${vault.client.id}")
    private String vaultClientId;

    @Value("${vault.client.key}")
    private String vaultClientKey;

    public String getSecret(final String secretName) {

        final KeyVaultClient client = new KeyVaultClient(new ClientSecretKeyVaultCredential(vaultClientId, vaultClientKey));

        final SecretBundle secretBundle = client.getSecret(vaultBaseUrl, secretName);

        return secretBundle.value();
    }
}