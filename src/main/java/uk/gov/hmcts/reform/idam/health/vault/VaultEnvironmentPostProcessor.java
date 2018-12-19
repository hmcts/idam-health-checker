package uk.gov.hmcts.reform.idam.health.vault;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor {

    protected static final String VAULT_BASE_URL = "azure.keyvault.uri";
    protected static final String VAULT_CLIENT_ID = "azure.keyvault.client-id";
    protected static final String VAULT_CLIENT_KEY = "azure.keyvault.client-key";
    protected static final String VAULT_MSI_URL = "azure.keyvault.msi.url";

    protected static final String VAULT_PROPERTIES = "vaultProperties";
    private static final String ACCESS_TOKEN_TYPE = "access_token";

    private static final Map<String, String> vaultKeyPropertyNames = ImmutableMap.of(
            "test-owner-username", "test.owner.username",
            "test-owner-password", "test.owner.password",
            "web-admin-client-secret", "web.admin.client.secret",
            "BINDPASSWD", "ldap.password",
            "appinsights-instrumentationkey", "azure.application-insights.instrumentation-key"
    );

    private final KeyVaultClientProvider provider;

    public VaultEnvironmentPostProcessor() {
        this(new KeyVaultClientProvider() {
            @Override
            public KeyVaultClient getClient(String clientId, String clientKey, String msiUrl) {
                if (StringUtils.isNoneEmpty(clientId, clientKey)) {
                    return new KeyVaultClient(new ClientSecretKeyVaultCredential(clientId, clientKey));
                } else if (StringUtils.isNotEmpty(msiUrl)) {
                    return new KeyVaultClient(new AccessTokenKeyVaultCredential(msiUrl));
                }
                return null;
            }
        });
    }

    public VaultEnvironmentPostProcessor(KeyVaultClientProvider provider) {
        this.provider = provider;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String vaultBaseUri = environment.getProperty(VAULT_BASE_URL);
        String vaultClientId = environment.getProperty(VAULT_CLIENT_ID);
        String vaultClientKey = environment.getProperty(VAULT_CLIENT_KEY);
        String vaultMsiUrl = environment.getProperty(VAULT_MSI_URL);

        KeyVaultClient client = provider.getClient(vaultClientId, vaultClientKey, vaultMsiUrl);

        if ((StringUtils.isNotEmpty(vaultBaseUri)) && (client != null)) {
            Properties props = new Properties();

            for (String vaultKey : vaultKeyPropertyNames.keySet()) {
                String value = loadValue(client, vaultBaseUri, vaultKey);
                if (value != null) {
                    System.out.println("Loaded vault key " + vaultKey);
                    props.put(vaultKeyPropertyNames.get(vaultKey), value);
                }
            }

            if (!props.isEmpty()) {
                environment.getPropertySources().addFirst(new PropertiesPropertySource(VAULT_PROPERTIES, props));
            }
        }
    }

    protected String loadValue(KeyVaultClient client, String vaultBaseUri, String key) {
        SecretBundle secretValue = client.getSecret(vaultBaseUri, key);
        if ((secretValue != null) && (secretValue.value() != null)) {
            return secretValue.value();
        }
        return null;
    }

}
