package uk.gov.hmcts.reform.idam.health.vault;

import org.springframework.core.env.ConfigurableEnvironment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class KeyVaultConfig {

    protected static final String VAULT_BASE_URL = "azure.keyvault.uri";
    protected static final String VAULT_CLIENT_ID = "azure.keyvault.client-id";
    protected static final String VAULT_CLIENT_KEY = "azure.keyvault.client-key";
    protected static final String VAULT_MSI_URL = "azure.keyvault.msi.url";

    private String vaultBaseUrl;

    private String vaultClientId;

    private String vaultClientKey;

    private String vaultMsiUrl;

    public KeyVaultConfig(ConfigurableEnvironment environment) {
        vaultBaseUrl = environment.getProperty(VAULT_BASE_URL);
        vaultClientId = environment.getProperty(VAULT_CLIENT_ID);
        vaultClientKey = environment.getProperty(VAULT_CLIENT_KEY);
        vaultMsiUrl = environment.getProperty(VAULT_MSI_URL);
    }
}
