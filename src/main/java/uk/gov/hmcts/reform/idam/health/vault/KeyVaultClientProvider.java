package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;

public interface KeyVaultClientProvider {

    KeyVaultClient getClient(KeyVaultConfig keyVaultConfig);

}
