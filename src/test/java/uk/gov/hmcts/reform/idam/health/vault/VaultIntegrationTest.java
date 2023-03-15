package uk.gov.hmcts.reform.idam.health.vault;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpStatus;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static uk.gov.hmcts.reform.idam.health.vault.EnvironmentKeyVaultConfigBuilder.VAULT_BASE_URL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VaultEnvironmentPostProcessor.class)
@ActiveProfiles("it")
public class VaultIntegrationTest {

    private static final int DUMMY_VAULT_SERVER_PORT = 9999;

    @Autowired
    ApplicationContext ctx;

    private static WireMockServer wireMockServer;

    protected static final Map<String, String> VAULT_PROPERTIES = ImmutableMap.<String, String>builder()
            .put("test-owner-username", "phil.space")
            .put("test-owner-password", "PasswOrd")
            .put("web-admin-client-secret", "secret")
            .put("DSUrootUserPassword", "ABCDE")
            .put("DSTrootUserPassword", "FGHIJ")
            .put("adminUID", "admin123")
            .put("adminPassword", "passw0rd12345")
            .put("appinsights-instrumentationkey", "ABCDE12345")
            .build();

    private final static String TOKEN_RESPONSE = "{\n"+
            "\t\"access_token\": \"eyJ0eXAiOiJKV1...hQ5J4_hoQ\",\n"+
            "\t\"client_id\": \"9b48de17-5f97-45ea-b4e8-912f60c95ba3\",\n"+
            "\t\"expires_in\": \"28800\",\n"+
            "\t\"expires_on\": \"1545076836\",\n"+
            "\t\"ext_expires_in\": \"28800\",\n"+
            "\t\"not_before\": \"1545047736\",\n"+
            "\t\"resource\": \"https://vault.azure.net\",\n"+
            "\t\"token_type\": \"Bearer\"\n"+
            "}";

    final static String SECRET_RESPONSE_TEMPLATE = "{\"value\":\"%s\",\n" +
            "\t\"id\":\"https://test.vault.azure.net/secrets/%s/5f5b24471cca47f99cdd3204d41372d2\",\n" +
            "\t\"attributes\":{\"enabled\":true,\"created\":1541609008,\"updated\":1541609008,\"recoveryLevel\":\"Purgeable\"},\n" +
            "\t\"tags\":{\"file-encoding\":\"utf-8\"}}";

    @BeforeClass
    public static void setup() {
        wireMockServer = new WireMockServer(options()
                .port(DUMMY_VAULT_SERVER_PORT)
                .extensions(ExampleTransformer.class));
        wireMockServer.start();

        configureFor("localhost", DUMMY_VAULT_SERVER_PORT);
        stubFor(get(urlPathMatching("/metadata/identity/oauth2/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TOKEN_RESPONSE)));

        stubFor(get(urlPathMatching("/test/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("secret-response-transformer")));
    }

    @AfterClass
    public static void shutdown() {
        wireMockServer.stop();
    }

    @Test
    public void loadVaultPropertiesUsingAccessToken() throws Exception {
        Environment env = ctx.getEnvironment();

        final String url = env.getProperty(VAULT_BASE_URL);
        assertThat(url, is("http://localhost:" + DUMMY_VAULT_SERVER_PORT + "/test"));

        final String username = env.getProperty("test.owner.username");
        final String userPwd = env.getProperty("test.owner.password");
        final String webAdmin = env.getProperty("web.admin.client.secret");
        final String ldapUserstorePwd = env.getProperty("ldap.userstore-password");
        final String ldapTokenstorePwd = env.getProperty("ldap.tokenstore-password");
        final String replicationUser = env.getProperty("replication.healthprobe.command.user");
        final String replicationPwd = env.getProperty("replication.healthprobe.command.password");
        final String instrumentationKey = env.getProperty("azure.application-insights.instrumentation-key");

        assertEquals(VAULT_PROPERTIES.get("test-owner-username"), username);
        assertEquals(VAULT_PROPERTIES.get("test-owner-password"), userPwd);
        assertEquals(VAULT_PROPERTIES.get("web-admin-client-secret"), webAdmin);
        assertEquals(VAULT_PROPERTIES.get("DSUrootUserPassword"), ldapUserstorePwd);
        assertEquals(VAULT_PROPERTIES.get("DSTrootUserPassword"), ldapTokenstorePwd);
        assertEquals(VAULT_PROPERTIES.get("adminUID"), replicationUser);
        assertEquals(VAULT_PROPERTIES.get("adminPassword"), replicationPwd);
        assertEquals(VAULT_PROPERTIES.get("appinsights-instrumentationkey"), instrumentationKey);
    }

    public static class ExampleTransformer extends ResponseDefinitionTransformer {

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {

            return new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(generateResponse(request.getUrl()))
                    .build();
        }

        private static String generateResponse(String path) {
            for (String key : VAULT_PROPERTIES.keySet()) {
                if (path.contains(key)) {
                    return String.format(SECRET_RESPONSE_TEMPLATE, VAULT_PROPERTIES.get(key), key);
                }
            }

            return String.format(SECRET_RESPONSE_TEMPLATE, "--", "--");
        }

        @Override
        public String getName() {
            return "secret-response-transformer";
        }

        @Override
        public boolean applyGlobally() {
            return false;
        }
    }
}
