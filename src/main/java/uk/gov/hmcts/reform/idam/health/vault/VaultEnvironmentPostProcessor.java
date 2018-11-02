package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String VAULT_BASE_URL = "azure.keyvault.uri";
    private static final String VAULT_CLIENT_ID = "azure.keyvault.client-id";
    private static final String VAULT_CLIENT_KEY = "azure.keyvault.client-key";

    private static final String VAULT_PROPERTIES = "vaultProperties";

    List<String> vaultKeys = Arrays.asList(
            "system-owner-username",
            "system-owner-password",
            "web-admin-client-secret",
            "BINDPASSWD",
            "appinsights-instrumentationkey"

    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String vaultBaseUri = environment.getProperty(VAULT_BASE_URL);
        String vaultClientId = environment.getProperty(VAULT_CLIENT_ID);
        String vaultClientKey = environment.getProperty(VAULT_CLIENT_KEY);
        if (StringUtils.isNoneEmpty(vaultBaseUri, vaultClientId, vaultClientKey)) {

            KeyVaultCredentials credentials = new ClientSecretKeyVaultCredential(vaultClientId, vaultClientKey);
            KeyVaultClient client = new KeyVaultClient(credentials);

            Properties props = new Properties();

            for (String vaultKey : vaultKeys) {
                String value = getValue(client, vaultBaseUri, vaultKey);
                if (value != null) {
                    props.put(getPropName(vaultKey), value);
                }
            }

            if (!props.isEmpty()) {
                environment.getPropertySources().addFirst(new PropertiesPropertySource(VAULT_PROPERTIES, props));
            }
        }
    }

    protected String getValue(KeyVaultClient client, String vaultBaseUri, String key) {
        SecretBundle secretValue = client.getSecret(vaultBaseUri, key);
        if ((secretValue != null) && (secretValue.value() != null)) {
            return secretValue.value();
        }
        return null;
    }

    protected String getPropName(String keyName) {
        if ("bindpasswd".equalsIgnoreCase(keyName)) {
            return "ldap.password";
        } else if ("appinsights-instrumentationkey".equalsIgnoreCase(keyName)) {
            return "azure.application-insights.instrumentation-key";
        }
        return keyName.replaceAll("-", ".");
    }

}
