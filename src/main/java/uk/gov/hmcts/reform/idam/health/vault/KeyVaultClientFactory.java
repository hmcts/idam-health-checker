package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import org.springframework.core.env.ConfigurableEnvironment;

public interface KeyVaultClientFactory {

    KeyVaultClient getClient(ConfigurableEnvironment environment);


}
