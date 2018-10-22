package com.amido.healthchecker.azure;

import com.amido.healthchecker.util.SecretHolder;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class AzureVaultServiceTest {

    private final String vaultBaseUrl = "https://somewhere.vault.azure.net/";

    AzureVaultService vaultService;
    SecretHolder secretHolder = new SecretHolder();

    @Mock
    KeyVaultClient mockKeyVaultClient;

    @Before
    public void setup() throws Exception {
        SecretBundle amSecretBundle = new SecretBundle().withValue("am-test");
        SecretBundle ldapSecretBundle = new SecretBundle().withValue("ldap-test");
        SecretBundle testSecretBundle = new SecretBundle().withValue("smoke-test");

        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "am-password")).thenReturn(amSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "smoke-test-user-username")).thenReturn(testSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "smoke-test-user-password")).thenReturn(testSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "cts-ldap-password")).thenReturn(ldapSecretBundle);
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "crs-ldap-password")).thenReturn(ldapSecretBundle);

        vaultService = new AzureVaultService(secretHolder, mockKeyVaultClient);

        Field field = ReflectionUtils.findField(AzureVaultService.class, "vaultBaseUrl");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, vaultService, vaultBaseUrl);
    }

    @Test
    public void shouldLoadAllSecretsInSecretHolder(){
        //when
        vaultService.loadAllSecrets();

        //then assert
        assertThat(secretHolder.getSecretsMap().size(), equalTo(5));
        assertThat(secretHolder.getAmPassword(), equalTo("am-test"));
        assertThat(secretHolder.getSmokeTestUserUsername(), equalTo("smoke-test"));
        assertThat(secretHolder.getSmokeTestUserPassword(), equalTo("smoke-test"));
        assertThat(secretHolder.getCrsLdapPassword(), equalTo("ldap-test"));
        assertThat(secretHolder.getCtsLdapPassword(), equalTo("ldap-test"));

        //then verify
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "am-password");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "smoke-test-user-username");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "smoke-test-user-password");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "cts-ldap-password");
        Mockito.verify(mockKeyVaultClient, times(1)).getSecret(vaultBaseUrl, "crs-ldap-password");
    }

    @Test(expected = IllegalStateException.class)
    public void missingValueThrowsException() {
        //given
        Mockito.when(mockKeyVaultClient.getSecret(vaultBaseUrl, "am-password")).thenReturn(null);

        //throws exception
        vaultService.loadAllSecrets();
    }
}