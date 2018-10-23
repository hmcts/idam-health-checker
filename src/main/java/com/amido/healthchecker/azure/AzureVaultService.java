package com.amido.healthchecker.azure;

import com.amido.healthchecker.util.SecretHolder;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Qualifier("vaultService")
@Profile("live")
public class AzureVaultService implements VaultService {

    @Value("${vault.base.url}")
    private String vaultBaseUrl;

    private KeyVaultClient client;
    private SecretHolder secretHolder;

    @Autowired
    public AzureVaultService(SecretHolder secretHolder, KeyVaultClient keyVaultClient){
        this.secretHolder = secretHolder;
        this.client = keyVaultClient;
    }

    @Override
    public void loadAllSecrets() {
        this.secretHolder.getSecretNames().forEach(name -> {
                    final SecretBundle secretBundle = client.getSecret(vaultBaseUrl, name);
                    if(secretBundle!=null) {
                        this.secretHolder.setSecretsMap(name, secretBundle.value());
                    } else {
                        throw new IllegalStateException("Couldn't find secret " + name);
                    }
                }
        );
    }
}