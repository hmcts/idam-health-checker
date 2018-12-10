package uk.gov.hmcts.reform.idam.health.vault.msi;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;

public class CustomAppServiceMSICredentials extends AzureTokenCredentials {

    private static final String REQUEST_HEADER_METADATA_VALUE_TRUE = "true";

    private final MSIProvider msiProvider;

    public CustomAppServiceMSICredentials(AzureEnvironment environment, MSIProvider msiProvider) {
        super(environment, null);
        this.msiProvider = msiProvider;
    }


    @Override
    public String getToken(String resource) {

        AccessTokenRespHolder accessTokenRespHolder
                = msiProvider.getMSIAccessToken(REQUEST_HEADER_METADATA_VALUE_TRUE);

        return accessTokenRespHolder.getAccessToken();
    }

}
