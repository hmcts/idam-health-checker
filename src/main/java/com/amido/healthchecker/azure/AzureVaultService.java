package com.amido.healthchecker.azure;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AzureVaultService implements VaultService {

    @Value("${vault.base.url}")
    private String vaultBaseUrl;

    @Value("${vault.client.id}")
    private String vaultClientId;

    @Value("${vault.client.key}")
    private String vaultClientKey;

    public void loadSecret(final String systemPropertyName, final String secretName) {

        final KeyVaultClient client = new KeyVaultClient(new ClientSecretKeyVaultCredential(vaultClientId, vaultClientKey));

        final SecretBundle secretBundle = client.getSecret(vaultBaseUrl, secretName);
        if (secretBundle != null) {
            System.setProperty(systemPropertyName, secretBundle.value());
        } else {
            throw new IllegalStateException("Couldn't find secret " + secretName);
        }
    }
}