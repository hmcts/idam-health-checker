package uk.gov.hmcts.reform.idam.health.vault;

import org.springframework.core.env.ConfigurableEnvironment;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class KeyVaultConfig {

    protected static final String VAULT_BASE_URL = "azure.keyvault.uri";
    protected static final String VAULT_CLIENT_ID = "azure.keyvault.client-id";
    protected static final String VAULT_CLIENT_KEY = "azure.keyvault.client-key";
    protected static final String VAULT_MSI_URL = "azure.keyvault.msi.url";
    protected static final String VAULT_ERROR_MAX_RETRIES = "azure.keyvault.msi.error.retry.max-number";
    protected static final String VAULT_ERROR_RETRY_INTERVAL_MILLIS = "azure.keyvault.msi.error.retry.interval-millis";

    private String vaultBaseUrl;

    private String vaultClientId;

    private String vaultClientKey;

    private String vaultMsiUrl;

    private int vaultErrorMaxRetries;

    private int vaultErrorRetryIntervalMillis;

    public KeyVaultConfig(ConfigurableEnvironment environment) {
        vaultBaseUrl = environment.getProperty(VAULT_BASE_URL);
        vaultClientId = environment.getProperty(VAULT_CLIENT_ID);
        vaultClientKey = environment.getProperty(VAULT_CLIENT_KEY);
        vaultMsiUrl = environment.getProperty(VAULT_MSI_URL);

        final String maxRetries = environment.getProperty(VAULT_ERROR_MAX_RETRIES);
        if (maxRetries != null) {
            vaultErrorMaxRetries = Integer.valueOf(maxRetries);
        }

        final String retryInterval = environment.getProperty(VAULT_ERROR_RETRY_INTERVAL_MILLIS);
        if (retryInterval != null) {
            vaultErrorRetryIntervalMillis = Integer.valueOf(retryInterval);
        }
     }
}
