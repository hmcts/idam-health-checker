package uk.gov.hmcts.reform.idam.healthchecker.azure;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.healthchecker.util.SecretHolder;

@Component
@Qualifier("vaultService")
@Profile("live")
@Slf4j
public class AzureVaultService implements VaultService {

    @Value("${vault.base.url}")
    private String vaultBaseUrl;

    private KeyVaultClient client;
    private SecretHolder secretHolder;

    @Autowired
    public AzureVaultService(SecretHolder secretHolder, KeyVaultClient keyVaultClient) {
        this.secretHolder = secretHolder;
        this.client = keyVaultClient;
    }

    @Override
    public void loadAllSecrets() {
        this.secretHolder.getSecretNames().forEach(name -> {
            final SecretBundle secretBundle = client.getSecret(vaultBaseUrl, name);
            if (secretBundle != null) {
                this.secretHolder.setSecretsMap(name, secretBundle.value());
            } else {
                throw new IllegalStateException("Couldn't find secret " + name);
            }
        });
    }
}