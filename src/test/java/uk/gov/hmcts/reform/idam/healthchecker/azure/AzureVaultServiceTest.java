package uk.gov.hmcts.reform.idam.healthchecker.azure;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.idam.healthchecker.util.AMSecretHolder;
import uk.gov.hmcts.reform.idam.healthchecker.util.DSTokenStoreSecretHolder;
import uk.gov.hmcts.reform.idam.healthchecker.util.DSUserStoreSecretHolder;
import uk.gov.hmcts.reform.idam.healthchecker.util.SecretHolder;

import java.lang.reflect.Field;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class AzureVaultServiceTest {

    private final String vaultBaseUrl = "https://somewhere.vault.azure.net/";

    private AMSecretHolder amSecretHolder = new AMSecretHolder("am-password","smoke-test-user-password");
    private DSTokenStoreSecretHolder dsTokenStoreSecretHolder = new DSTokenStoreSecretHolder("ds-token-store-password");
    private DSUserStoreSecretHolder dsUserStoreSecretHolder = new DSUserStoreSecretHolder("ds-user-store-password");

    private AzureVaultService vaultService;
    private SecretHolder secretHolder = new SecretHolder(amSecretHolder, dsTokenStoreSecretHolder, dsUserStoreSecretHolder);

    @Mock
    private KeyVaultClient mockKeyVaultClient;

    @Before
    public void setup() {
        SecretBundle amSecretBundle = new SecretBundle().withValue("am-test");
        SecretBundle ldapSecretBundle = new SecretBundle().withValue("ldap-test");
        SecretBundle testSecretBundle = new SecretBundle().withValue("smoke-test");

        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "am-password")).thenReturn(amSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "smoke-test-user-password")).thenReturn(testSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "ds-token-store-password")).thenReturn(ldapSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "ds-user-store-password")).thenReturn(ldapSecretBundle);

        vaultService = new AzureVaultService(secretHolder, mockKeyVaultClient);

        Field field = ReflectionUtils.findField(AzureVaultService.class, "vaultBaseUrl");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, vaultService, vaultBaseUrl);
    }

    @Test
    public void shouldLoadAllSecretsInSecretHolder() {
        //when
        vaultService.loadAllSecrets();

        //then assert
        assertThat(secretHolder.getSecretsMap().size(), equalTo(4));
        assertThat(secretHolder.getAmPassword(), equalTo("am-test"));
        assertThat(secretHolder.getSmokeTestUserPassword(), equalTo("smoke-test"));
        assertThat(secretHolder.getDSUserStorePassword(), equalTo("ldap-test"));
        assertThat(secretHolder.getDSTokenStorePassword(), equalTo("ldap-test"));

        //then verify
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "am-password");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "smoke-test-user-password");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "ds-token-store-password");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "ds-user-store-password");
    }

    @Test(expected = IllegalStateException.class)
    public void missingValueThrowsException() {
        //given
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "am-password")).thenReturn(null);

        //throws exception
        vaultService.loadAllSecrets();
    }
}