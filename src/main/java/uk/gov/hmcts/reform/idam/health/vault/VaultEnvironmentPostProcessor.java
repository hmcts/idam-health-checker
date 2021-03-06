package uk.gov.hmcts.reform.idam.health.vault;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.vault.config.KeyVaultClientProvider;
import uk.gov.hmcts.reform.vault.config.KeyVaultConfig;
import uk.gov.hmcts.reform.vault.credential.AccessTokenKeyVaultCredential;
import uk.gov.hmcts.reform.vault.credential.ClientSecretKeyVaultCredential;

import java.util.Map;
import java.util.Properties;

@CustomLog
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor {

    protected static final String VAULT_PROPERTIES = "vaultProperties";

    private static final Map<String, String> vaultKeyPropertyNames = ImmutableMap.<String, String>builder()
            .put("openidm-username", "idm.healthprobe.ldapCheck.username")
            .put("openidm-password", "idm.healthprobe.ldapCheck.password")
            .put("test-owner-username", "test.owner.username")
            .put("test-owner-password", "test.owner.password")
            .put("web-admin-client-secret", "web.admin.client.secret")
            .put("BINDPASSWD", "ldap.password")
            .put("adminUID", "replication.healthprobe.command.user")
            .put("adminPassword", "replication.healthprobe.command.password")
            .put("appinsights-instrumentationkey", "azure.application-insights.instrumentation-key").build();

    private final KeyVaultClientProvider provider;

    private KeyVaultConfig keyVaultConfig;

    public VaultEnvironmentPostProcessor() {
        this(new KeyVaultClientProvider() {
            @Override
            public KeyVaultClient getClient(KeyVaultConfig keyVaultConfig) {
                if (StringUtils.isNoneEmpty(keyVaultConfig.getVaultClientId(), keyVaultConfig.getVaultClientKey())) {
                    return new KeyVaultClient(new ClientSecretKeyVaultCredential(keyVaultConfig.getVaultClientId(), keyVaultConfig.getVaultClientKey()));
                } else if (StringUtils.isNotEmpty(keyVaultConfig.getVaultMsiUrl())) {
                    return new KeyVaultClient(new AccessTokenKeyVaultCredential(keyVaultConfig.getVaultMsiUrl(),
                            keyVaultConfig.getVaultErrorMaxRetries(), keyVaultConfig.getVaultErrorRetryIntervalMillis()));
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
        keyVaultConfig = new EnvironmentKeyVaultConfigBuilder(environment).build();

        KeyVaultClient client = provider.getClient(keyVaultConfig);

        if ((StringUtils.isNotEmpty(keyVaultConfig.getVaultBaseUrl())) && (client != null)) {
            Properties props = new Properties();

            for (String vaultKey : vaultKeyPropertyNames.keySet()) {
                String value = loadValue(client, keyVaultConfig.getVaultBaseUrl(), vaultKey);
                if (value != null) {
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
