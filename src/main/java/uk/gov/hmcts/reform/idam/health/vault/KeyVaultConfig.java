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
    protected static final String VAULT_ERROR_MAX_RETRIES = "azure.keyvault.msi.error.retry.max-number";
    protected static final String VAULT_ERROR_RETRY_INTERVAL_MILLIS = "azure.keyvault.msi.error.retry.interval-millis";

    protected static final String DEFAULT_VAULT_ERROR_MAX_RETRIES = "3";
    protected static final String DEFAULT_VAULT_ERROR_RETRY_INTERVAL_MILLIS = "0";

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
        vaultErrorMaxRetries = Integer.valueOf(environment.getProperty(VAULT_ERROR_MAX_RETRIES, DEFAULT_VAULT_ERROR_MAX_RETRIES));
        vaultErrorRetryIntervalMillis = Integer.valueOf(environment.getProperty(VAULT_ERROR_RETRY_INTERVAL_MILLIS, DEFAULT_VAULT_ERROR_RETRY_INTERVAL_MILLIS));
    }
}
