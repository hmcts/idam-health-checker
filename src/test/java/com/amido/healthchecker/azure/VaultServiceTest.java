package com.amido.healthchecker.azure;

import com.amido.healthchecker.util.AMSecretHolder;
import com.amido.healthchecker.util.DSTokenStoreSecretHolder;
import com.amido.healthchecker.util.DSUserStoreSecretHolder;
import com.amido.healthchecker.util.SecretHolder;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


public class VaultServiceTest {

    private VaultService service;
    private SecretHolder secretHolder;
    private Environment mockEnvironment;

    private AMSecretHolder amSecretHolder = new AMSecretHolder("am-password", "smoke-test-user-password");
    private DSTokenStoreSecretHolder dsTokenStoreSecretHolder = new DSTokenStoreSecretHolder("ds-token-store-password");
    private DSUserStoreSecretHolder dsUserStoreSecretHolder = new DSUserStoreSecretHolder("ds-user-store-password");

    @Before
    public void setup() {

        mockEnvironment = Mockito.mock(Environment.class);
        when(mockEnvironment.getProperty("am-password")).thenReturn("test");
        when(mockEnvironment.getProperty("smoke-test-user-username")).thenReturn("testr@test.net");
        when(mockEnvironment.getProperty("smoke-test-user-password")).thenReturn("password");
        when(mockEnvironment.getProperty("ds-token-store-password")).thenReturn("Pa55word11");
        when(mockEnvironment.getProperty("ds-user-store-password")).thenReturn("Pa55word11");

        secretHolder = new SecretHolder(amSecretHolder, dsTokenStoreSecretHolder, dsUserStoreSecretHolder);

        service = new DummyVaultService(mockEnvironment, secretHolder);
    }

    @Test(expected = IllegalStateException.class)
    public void missingValueThrowsException() {
        //given
        when(mockEnvironment.getProperty("am-password")).thenReturn("");

        //throws exception
        service.loadAllSecrets();
    }

    @Test
    public void shouldUpdateSecretHolder() {
        //when
        service.loadAllSecrets();

        //then
        assertThat(secretHolder.getSecretsMap().size(), equalTo(4));
        assertThat(secretHolder.getAmPassword(), equalTo("test"));
        assertThat(secretHolder.getSmokeTestUserPassword(), equalTo("password"));
        assertThat(secretHolder.getDSUserStorePassword(), equalTo("Pa55word11"));
        assertThat(secretHolder.getDSTokenStorePassword(), equalTo("Pa55word11"));
    }

}
