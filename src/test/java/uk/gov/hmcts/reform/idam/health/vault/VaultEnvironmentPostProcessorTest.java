package uk.gov.hmcts.reform.idam.health.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VaultEnvironmentPostProcessorTest {

    @Mock
    private ConfigurableEnvironment configurableEnvironment;

    @Mock
    private SpringApplication springApplication;

    @Mock
    private KeyVaultClientProvider keyVaultClientProvider;

    @Mock
    private KeyVaultClient keyVaultClient;

    @Mock
    private MutablePropertySources propertySources;

    @Captor
    private ArgumentCaptor<PropertySource> propertySourceArgumentCaptor;

    private VaultEnvironmentPostProcessor postProcessor;

    @Before
    public void setup() {
        postProcessor = new VaultEnvironmentPostProcessor(keyVaultClientProvider);
    }

    @Test
    public void testPostProcessEnvironment_WithoutVaultProperties() {

        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_BASE_URL)).thenReturn(null);
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_CLIENT_ID)).thenReturn(null);
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_CLIENT_KEY)).thenReturn(null);
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_MSI_URL)).thenReturn(null);

        postProcessor.postProcessEnvironment(configurableEnvironment, springApplication);

        verify(keyVaultClientProvider, never()).getClient(anyString(), anyString(), anyString());
        verify(configurableEnvironment, never()).getPropertySources();
    }

    @Test
    public void testPostProcessEnvironment_WithoutVaultKeyValues() {

        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_BASE_URL)).thenReturn("test-vault-url");
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_CLIENT_ID)).thenReturn("test-vault-id");
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_CLIENT_KEY)).thenReturn("test-vault-key");
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_MSI_URL)).thenReturn("http://some/url");

        when(keyVaultClientProvider.getClient("test-vault-id", "test-vault-key", "http://some/url")).thenReturn(keyVaultClient);

        when(keyVaultClient.getSecret("test-vault-url", "test-owner-username")).thenReturn(null);
        when(keyVaultClient.getSecret("test-vault-url", "test-owner-password")).thenReturn(null);
        when(keyVaultClient.getSecret("test-vault-url", "web-admin-client-secret")).thenReturn(null);
        when(keyVaultClient.getSecret("test-vault-url", "BINDPASSWD")).thenReturn(null);
        when(keyVaultClient.getSecret("test-vault-url", "appinsights-instrumentationkey")).thenReturn(null);

        postProcessor.postProcessEnvironment(configurableEnvironment, springApplication);

        verify(configurableEnvironment, never()).getPropertySources();
    }

    @Test
    public void testPostProcessEnvironment_WithAllVaultKeyValues() {

        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_BASE_URL)).thenReturn("test-vault-url");
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_CLIENT_ID)).thenReturn("test-vault-id");
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_CLIENT_KEY)).thenReturn("test-vault-key");
        when(configurableEnvironment.getProperty(VaultEnvironmentPostProcessor.VAULT_MSI_URL)).thenReturn("http://some/url");
        when(configurableEnvironment.getPropertySources()).thenReturn(propertySources);

        when(keyVaultClientProvider.getClient("test-vault-id", "test-vault-key", "http://some/url")).thenReturn(keyVaultClient);

        when(keyVaultClient.getSecret("test-vault-url", "test-owner-username")).thenReturn(new SecretBundle().withValue("test-username"));
        when(keyVaultClient.getSecret("test-vault-url", "test-owner-password")).thenReturn(new SecretBundle().withValue("test-password"));
        when(keyVaultClient.getSecret("test-vault-url", "web-admin-client-secret")).thenReturn(new SecretBundle().withValue("test-secret"));
        when(keyVaultClient.getSecret("test-vault-url", "BINDPASSWD")).thenReturn(new SecretBundle().withValue("test-ldappass"));
        when(keyVaultClient.getSecret("test-vault-url", "appinsights-instrumentationkey")).thenReturn(new SecretBundle().withValue("test-instrumentation"));

        postProcessor.postProcessEnvironment(configurableEnvironment, springApplication);

        verify(propertySources).addFirst(propertySourceArgumentCaptor.capture());

        PropertySource propertySource = propertySourceArgumentCaptor.getValue();
        assertThat(propertySource.getProperty("test.owner.username"), is("test-username"));
        assertThat(propertySource.getProperty("test.owner.password"), is("test-password"));
        assertThat(propertySource.getProperty("web.admin.client.secret"), is("test-secret"));
        assertThat(propertySource.getProperty("ldap.password"), is("test-ldappass"));
        assertThat(propertySource.getProperty("azure.application-insights.instrumentation-key"), is("test-instrumentation"));
    }
}
