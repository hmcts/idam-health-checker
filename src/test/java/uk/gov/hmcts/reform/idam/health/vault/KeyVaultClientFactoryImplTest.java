package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.utils.Asserts;
import org.springframework.core.env.ConfigurableEnvironment;
import uk.gov.hmcts.reform.idam.health.vault.msi.AccessTokenRespHolder;
import uk.gov.hmcts.reform.idam.health.vault.msi.MSIProvider;

import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.idam.health.vault.KeyVaultClientFactoryImpl.VAULT_CLIENT_ID;
import static uk.gov.hmcts.reform.idam.health.vault.KeyVaultClientFactoryImpl.VAULT_CLIENT_KEY;

@RunWith(MockitoJUnitRunner.class)
public class KeyVaultClientFactoryImplTest {

    @Mock
    MSIProvider msiProvider;

    @Mock
    ConfigurableEnvironment environment;


    KeyVaultClientFactoryImpl keyVaultClientProviderImpl;

    @Before
    public void setup(){
        keyVaultClientProviderImpl = new KeyVaultClientFactoryImpl(msiProvider);
    }


    @Test
    public void shouldGetClientWithMSICredentialWhenMSIdentityEnabled() {
        //given
        AccessTokenRespHolder tokenRespHolder = new AccessTokenRespHolder();
        when(msiProvider.getMSIAccessToken("true")).thenReturn(tokenRespHolder);
        verify(environment, never()).getProperty(VAULT_CLIENT_ID);
        verify(environment, never()).getProperty(VAULT_CLIENT_KEY);

        //when
        KeyVaultClient client = keyVaultClientProviderImpl.getClient(environment);

        //then
        Asserts.assertNotNull(client, "client not instantiated");

    }

    @Test
    public void shouldGetKeyVaultClientWithClientCredentialWhenMSIdentityNotEnabled() {
        //given
        when(msiProvider.getMSIAccessToken("true")).thenThrow(new RuntimeException());
        when(environment.getProperty(VAULT_CLIENT_ID)).thenReturn("some-client-id");
        when(environment.getProperty(VAULT_CLIENT_KEY)).thenReturn("some-client-key");

        //when
        KeyVaultClient client = keyVaultClientProviderImpl.getClient(environment);

        //then
        Asserts.assertNotNull(client, "client not instantiated");

    }
}