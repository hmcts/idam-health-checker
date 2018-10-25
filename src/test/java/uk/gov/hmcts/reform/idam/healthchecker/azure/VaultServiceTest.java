package uk.gov.hmcts.reform.idam.healthchecker.azure;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.reform.idam.healthchecker.util.AMSecretHolder;
import uk.gov.hmcts.reform.idam.healthchecker.util.DSTokenStoreSecretHolder;
import uk.gov.hmcts.reform.idam.healthchecker.util.DSUserStoreSecretHolder;
import uk.gov.hmcts.reform.idam.healthchecker.util.SecretHolder;

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
        when(mockEnvironment.getProperty("ds-token-store-password")).thenReturn("somePassword");
        when(mockEnvironment.getProperty("ds-user-store-password")).thenReturn("somePassword");

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
        assertThat(secretHolder.getDSUserStorePassword(), equalTo("somePassword"));
        assertThat(secretHolder.getDSTokenStorePassword(), equalTo("somePassword"));
    }

}
