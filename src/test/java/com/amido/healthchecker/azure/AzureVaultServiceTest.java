package com.amido.healthchecker.azure;

import com.amido.healthchecker.util.AMSecretHolder;
import com.amido.healthchecker.util.DSTokenStoreSecretHolder;
import com.amido.healthchecker.util.DSUserStoreSecretHolder;
import com.amido.healthchecker.util.SecretHolder;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;

public class AzureVaultServiceTest {


    private final String vaultBaseUrl = "https://somewhere.vault.azure.net/";

    private AMSecretHolder amSecretHolder = new AMSecretHolder("am-passwordName","smoke-test-user-passwordName");
    private DSTokenStoreSecretHolder dsTokenStoreSecretHolder = new DSTokenStoreSecretHolder("ds-token-store-passwordName");
    private DSUserStoreSecretHolder dsUserStoreSecretHolder = new DSUserStoreSecretHolder("ds-user-store-passwordName");

    private AzureVaultService vaultService;
    private SecretHolder secretHolder = new SecretHolder(amSecretHolder, dsTokenStoreSecretHolder, dsUserStoreSecretHolder);
    private KeyVaultClient mockKeyVaultClient;


    @Before
    public void setup(){
        mockKeyVaultClient = Mockito.mock(KeyVaultClient.class);

        SecretBundle amSecretBundle = new SecretBundle().withValue("am-test");
        SecretBundle ldapSecretBundle = new SecretBundle().withValue("ldap-test");
        SecretBundle testSecretBundle = new SecretBundle().withValue("smoke-test");

        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "am-passwordName")).thenReturn(amSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "smoke-test-user-username")).thenReturn(testSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "smoke-test-user-passwordName")).thenReturn(testSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "ds-token-store-passwordName")).thenReturn(ldapSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "ds-user-store-passwordName")).thenReturn(ldapSecretBundle);

        vaultService = new AzureVaultService(secretHolder, mockKeyVaultClient);
        vaultService.setVaultBaseUrl(vaultBaseUrl);
    }

    @Test
    public void shouldLoadAllSecretsInSecretHolder(){
        //when
        vaultService.loadAllSecrets();

        //then assert
        assertThat(secretHolder.getSecretsMap().size(), equalTo(4));
        assertThat(secretHolder.getAmPassword(), equalTo("am-test"));
        assertThat(secretHolder.getSmokeTestUserPassword(), equalTo("smoke-test"));
        assertThat(secretHolder.getDSUserStorePassword(), equalTo("ldap-test"));
        assertThat(secretHolder.getDSTokenStorePassword(), equalTo("ldap-test"));

        //then verify
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "am-passwordName");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "smoke-test-user-passwordName");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "ds-token-store-passwordName");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "ds-user-store-passwordName");

    }

    @Test(expected = IllegalStateException.class)
    public void missingValueThrowsException() {
        //given
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "am-passwordName")).thenReturn(null);

        //throws exception
        vaultService.loadAllSecrets();
    }


}