package uk.gov.hmcts.reform.idam.health.vault.msi;

import com.microsoft.azure.AzureEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomAppServiceMSICredentialsTest {

    private static final String REQUEST_HEADER_METADATA_VALUE_TRUE = "true";
    private static final String ACCESS_TOKEN_SOME_ACCESS_TOKEN_VALUE ="some-access-token-value";

    private CustomAppServiceMSICredentials customAppServiceMSICredentials;

    @Mock
    MSIProvider msiProvider;

    @Before
    public void setup() {
        customAppServiceMSICredentials
                = new CustomAppServiceMSICredentials(AzureEnvironment.AZURE, msiProvider);
    }

    @Test
    public void shouldCallRequestExecuteMethodAndParseToken() {

        AccessTokenRespHolder accessTokenRespHolder = new AccessTokenRespHolder();
        accessTokenRespHolder.setAccessToken(ACCESS_TOKEN_SOME_ACCESS_TOKEN_VALUE);

        when(msiProvider.getMSIAccessToken(REQUEST_HEADER_METADATA_VALUE_TRUE)).thenReturn(accessTokenRespHolder);

        String token = customAppServiceMSICredentials.getToken("resource");

        Assert.assertEquals(token, ACCESS_TOKEN_SOME_ACCESS_TOKEN_VALUE);

    }

}
