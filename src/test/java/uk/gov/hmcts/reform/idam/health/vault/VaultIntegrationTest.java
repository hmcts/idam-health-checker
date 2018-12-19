package uk.gov.hmcts.reform.idam.health.vault;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static uk.gov.hmcts.reform.idam.health.vault.VaultEnvironmentPostProcessor.VAULT_BASE_URL;
import static uk.gov.hmcts.reform.idam.health.vault.DummyVaultServer.VAULT_PROPERTIES;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VaultEnvironmentPostProcessor.class)
@ActiveProfiles("it")
public class VaultIntegrationTest {

    @Autowired
    ApplicationContext ctx;

    private static final int DUMMY_VAULT_SERVER_PORT = 9999;

    @BeforeClass
    public static void setup() {
        // Start dummy server to emulate Azure Vault calls
        DummyVaultServer server = new DummyVaultServer(DUMMY_VAULT_SERVER_PORT);
    }

    @Test
    public void loadVaultProperties() throws Exception {
        Environment env = ctx.getEnvironment();

        final String url = env.getProperty(VAULT_BASE_URL);
        assertThat(url, is("http://localhost:" + DUMMY_VAULT_SERVER_PORT + "/test"));

        final String username = env.getProperty("test.owner.username");
        final String userPwd = env.getProperty("test.owner.password");
        final String webAdmin = env.getProperty("web.admin.client.secret");
        final String ldapPwd = env.getProperty("ldap.password");
        final String instrumentationKey = env.getProperty("azure.application-insights.instrumentation-key");

        assertEquals(VAULT_PROPERTIES.get("test-owner-username"), username);
        assertEquals(VAULT_PROPERTIES.get("test-owner-password"), userPwd);
        assertEquals(VAULT_PROPERTIES.get("web-admin-client-secret"), webAdmin);
        assertEquals(VAULT_PROPERTIES.get("BINDPASSWD"), ldapPwd);
        assertEquals(VAULT_PROPERTIES.get("appinsights-instrumentationkey"), instrumentationKey);
    }
}
