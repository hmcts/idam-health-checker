package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import uk.gov.hmcts.reform.idam.health.vault.msi.CustomAppServiceMSICredentials;

public interface KeyVaultClientProvider {

    KeyVaultClient getClient(CustomAppServiceMSICredentials credentials);

}
