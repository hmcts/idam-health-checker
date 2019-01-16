package uk.gov.hmcts.reform.idam.health.vault;

import org.springframework.core.env.ConfigurableEnvironment;
import uk.gov.hmcts.reform.vault.config.KeyVaultConfig;

public class EnvironmentKeyVaultConfigBuilder {

    public static final String VAULT_BASE_URL = "azure.keyvault.uri";
    public static final String VAULT_CLIENT_ID = "azure.keyvault.client-id";
    public static final String VAULT_CLIENT_KEY = "azure.keyvault.client-key";
    public static final String VAULT_MSI_URL = "azure.keyvault.msi.url";
    public static final String VAULT_ERROR_MAX_RETRIES = "azure.keyvault.msi.error.retry.max-number";
    public static final String VAULT_ERROR_RETRY_INTERVAL_MILLIS = "azure.keyvault.msi.error.retry.interval-millis";

    private ConfigurableEnvironment environment;

    public EnvironmentKeyVaultConfigBuilder(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    public KeyVaultConfig build() {
        KeyVaultConfig config = new KeyVaultConfig();
        config.setVaultBaseUrl(getProperty(VAULT_BASE_URL));
        config.setVaultClientId(getProperty(VAULT_CLIENT_ID));
        config.setVaultClientKey(getProperty(VAULT_CLIENT_KEY));
        config.setVaultMsiUrl(getProperty(VAULT_MSI_URL));
        config.setVaultErrorMaxRetries(getIntProperty(VAULT_ERROR_MAX_RETRIES));
        config.setVaultErrorRetryIntervalMillis(getIntProperty(VAULT_ERROR_RETRY_INTERVAL_MILLIS));

        return config;
    }

    private String getProperty(String propertyName) {
        return environment.getProperty(propertyName);
    }

    private int getIntProperty(String propertyName) {
        String propertyValue = environment.getProperty(propertyName);
        if (propertyValue != null) {
            return  Integer.valueOf(propertyValue);
        }

        return 0;
    }
}

