package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.keyvault.KeyVaultClient;
import feign.Feign;
import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import uk.gov.hmcts.reform.idam.health.vault.msi.CustomRequestRetryer;
import uk.gov.hmcts.reform.idam.health.vault.msi.AccessTokenRespHolder;
import uk.gov.hmcts.reform.idam.health.vault.msi.CustomAppServiceMSICredentials;
import uk.gov.hmcts.reform.idam.health.vault.msi.MSIProvider;


@Slf4j
public class KeyVaultClientFactoryImpl implements KeyVaultClientFactory {

    private static final String TAG = "KeyVaultClient : ";

    protected static final String VAULT_CLIENT_ID = "azure.keyvault.client-id";
    protected static final String VAULT_CLIENT_KEY = "azure.keyvault.client-key";
    protected static final String VNET_URL = "http://169.254.169.254/metadata";

    private final MSIProvider msiProvider;

    public KeyVaultClientFactoryImpl() {
        this.msiProvider = getMSIProvider();
    }

    protected KeyVaultClientFactoryImpl(MSIProvider msiProvider) {
        this.msiProvider = msiProvider;
    }

    @Override
    public KeyVaultClient getClient(ConfigurableEnvironment environment) {

        KeyVaultClient client = getKeyCredentialClient(environment);

        if(client == null) {
            try {
                AccessTokenRespHolder accessTokenRespHolder = msiProvider.getMSIAccessToken("true");
                if(accessTokenRespHolder != null) {
                    client = new KeyVaultClient(new CustomAppServiceMSICredentials(AzureEnvironment.AZURE, msiProvider));
                }

            } catch (Exception ex) {
                log.error(TAG + "Managed Service Identity not enabled");
            }
        }

       return client;

    }

    private KeyVaultClient getKeyCredentialClient(ConfigurableEnvironment environment) {
        String vaultClientId = environment.getProperty(VAULT_CLIENT_ID);
        String vaultClientKey = environment.getProperty(VAULT_CLIENT_KEY);

        if(StringUtils.isNoneEmpty(vaultClientId, vaultClientId)) {
            return new KeyVaultClient(new ClientSecretKeyVaultCredential(vaultClientId, vaultClientKey));
        }

        log.info(TAG + "vault clientId/clientKey not found");

        return null;
    }


    protected MSIProvider getMSIProvider() {
        return Feign.builder()
                .retryer(new CustomRequestRetryer())
                .target(getTarget());
    }

    private Target<MSIProvider> getTarget(){
        return new Target.HardCodedTarget<MSIProvider>
                (MSIProvider.class, "msiProvider", VNET_URL);
    }


}
