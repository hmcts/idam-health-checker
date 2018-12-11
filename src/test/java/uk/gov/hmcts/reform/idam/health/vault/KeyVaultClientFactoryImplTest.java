package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
    public void shouldGetClientWhenCLientIdAndClientKeyPresent() {
        //given
        when(environment.getProperty(VAULT_CLIENT_ID)).thenReturn("some-client-id");
        when(environment.getProperty(VAULT_CLIENT_KEY)).thenReturn("some-client-key");
        verify(msiProvider, never()).getMSIAccessToken("true");

        //when
        KeyVaultClient client = keyVaultClientProviderImpl.getClient(environment);

        //then
        Assert.assertNotNull(client);

    }


    @Test
    public void shouldGetClientWithMSICredentialWhenMSIdentityEnabled() {
        //given
        when(environment.getProperty(VAULT_CLIENT_ID)).thenReturn(null);
        when(environment.getProperty(VAULT_CLIENT_KEY)).thenReturn(null);
        AccessTokenRespHolder tokenRespHolder = new AccessTokenRespHolder();
        when(msiProvider.getMSIAccessToken("true")).thenReturn(tokenRespHolder);

        //when
        KeyVaultClient client = keyVaultClientProviderImpl.getClient(environment);

        //then
        Assert.assertNotNull(client);

    }

    @Test
    public void shouldNotGetAnyKeyVaultClientWhenClientIdandClientkeyNotSetAndMSINotEnabled() {
        //given
        when(environment.getProperty(VAULT_CLIENT_ID)).thenReturn("");
        when(environment.getProperty(VAULT_CLIENT_KEY)).thenReturn("");
        when(msiProvider.getMSIAccessToken("true")).thenThrow(new RuntimeException());

        //when
        KeyVaultClient client = keyVaultClientProviderImpl.getClient(environment);

        //then
        Assert.assertNull(client);
    }

    @Test
    public void shouldNotGetKeyVaultClientWhenMSINotReturnedAccessToken() {
        //given
        when(environment.getProperty(VAULT_CLIENT_ID)).thenReturn("");
        when(environment.getProperty(VAULT_CLIENT_KEY)).thenReturn("");
        when(msiProvider.getMSIAccessToken("true")).thenReturn(null);

        //when
        KeyVaultClient client = keyVaultClientProviderImpl.getClient(environment);

        //then
        Assert.assertNull(client);

    }
}