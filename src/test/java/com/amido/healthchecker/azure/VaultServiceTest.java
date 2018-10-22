package com.amido.healthchecker.azure;

import com.amido.healthchecker.util.SecretHolder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class VaultServiceTest {

    VaultService service;
    SecretHolder secretHolder;
    Environment mockEnvironment;

    @Before
    public void setup() {

        mockEnvironment = Mockito.mock(Environment.class);
        when(mockEnvironment.getProperty("am-password")).thenReturn("test");
        when(mockEnvironment.getProperty("smoke-test-user-username")).thenReturn("testr@test.net");
        when(mockEnvironment.getProperty("smoke-test-user-password")).thenReturn("password");
        when(mockEnvironment.getProperty("cts-ldap-password")).thenReturn("Pa55word11");
        when(mockEnvironment.getProperty("crs-ldap-password")).thenReturn("Pa55word11");

        secretHolder = new SecretHolder();

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
    public void shouldUpdateSecretHolder(){
        //when
        service.loadAllSecrets();

        //then
        assertThat(secretHolder.getSecretsMap().size(), equalTo(5));
        assertThat(secretHolder.getAmPassword(), equalTo("test"));
        assertThat(secretHolder.getSmokeTestUserUsername(), equalTo("testr@test.net"));
        assertThat(secretHolder.getSmokeTestUserPassword(), equalTo("password"));
        assertThat(secretHolder.getCrsLdapPassword(), equalTo("Pa55word11"));
        assertThat(secretHolder.getCtsLdapPassword(), equalTo("Pa55word11"));
    }

}
