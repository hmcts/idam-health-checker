package uk.gov.hmcts.reform.idam.health;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class PackagedRuntimeTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path REPORTS = PublishedArtifactTest.BUILD.resolve("reports/runtime");
    private static final List<String> AM_COMPONENTS = List.of("amLiveScheduledHealthProbe",
            "amReadyScheduledHealthProbe", "amPasswordGrantScheduledHealthProbe", "amRootPasswordGrantScheduledHealthProbe");

    @ParameterizedTest(name = "{0} returns {1}/{2} and deployment info")
    @CsvSource({"optimist, 200, UP", "pessimist, 503, DOWN"})
    void fixedHealthAndInfo(String profile, int code, String status) throws Exception {
        try (var app = new Application(profile, List.of())) {
            app.health(profile, code, status, body -> {
                assertEquals(Set.of(profile), JSON.convertValue(body.path("components"), java.util.Map.class).keySet());
                assertTrue(body.path("groups").isMissingNode() || body.path("groups").isEmpty());
            });
            var info = app.get("/admin/info");
            assertEquals(200, info.statusCode());
            var body = JSON.readTree(info.body());
            assertEquals("ci-forgerock-fixture", body.at("/forgerock/commit").asString());
            String commit = body.at("/git/commit/id").asString();
            assertFalse(commit.isBlank(), "Missing Git commit");
            assertTrue(PublishedArtifactTest.COMMIT.startsWith(commit), "Git commit mismatch");
        }
    }

    @Test
    void amFormsTimestampsFailureAndRecovery() throws Exception {
        var am = new WireMockServer(options().bindAddress("127.0.0.1").dynamicPort());
        am.start();
        try {
            am.stubFor(get(urlEqualTo("/json/health/live")).willReturn(okJson("{}")));
            var ready = am.stubFor(get(urlEqualTo("/json/health/ready")).willReturn(okJson("{}")));
            am.stubFor(tokenForm("/oauth2/realms/hmcts/access_token")
                    .withBasicAuth("ci-agent", "ci-secret").willReturn(okJson("{\"access_token\":\"ci-token\"}")));
            am.stubFor(tokenForm("/oauth2/access_token")
                    .withFormParam("client_id", equalTo("ci-client"))
                    .withFormParam("client_secret", equalTo("default"))
                    .willReturn(okJson("{\"access_token\":\"ci-token\"}")));
            var args = new ArrayList<>(List.of("--am.root=" + am.baseUrl(),
                    "--test.owner.username=ci user+&", "--test.owner.password=ci pass+&=",
                    "--web.admin.client.name=ci-agent", "--web.admin.client.secret=ci-secret",
                    "--am.healthprobe.root-password-grant.am-user=ci user+&",
                    "--am.healthprobe.root-password-grant.am-password=ci pass+&=",
                    "--am.healthprobe.root-password-grant.client-id=ci-client",
                    "--am.healthprobe.root-password-grant.client-scope=openid profile roles"));
            for (String probe : List.of("live", "ready", "password-grant", "root-password-grant")) {
                args.add("--am.healthprobe." + probe + ".check-interval=250ms");
                args.add("--am.healthprobe." + probe + ".freshness-interval=1s");
            }
            try (var app = new Application("am", args)) {
                app.health("am-up", 200, "UP", PackagedRuntimeTest::assertAmUp);
                am.editStub(get(urlEqualTo("/json/health/ready")).withId(ready.getId()).willReturn(aResponse().withStatus(503)));
                app.health("am-down", 503, "DOWN", body -> {
                    var component = body.path("components").path("amReadyScheduledHealthProbe");
                    assertEquals("DOWN", component.path("status").asString());
                    assertTrue(component.at("/details/AMReadyHealthProbe").asString().contains("503"));
                    assertDoesNotThrow(() -> Instant.parse(component.at("/details/lastDetailUpdate").asString()));
                });
                am.editStub(get(urlEqualTo("/json/health/ready")).withId(ready.getId()).willReturn(okJson("{}")));
                app.health("am-recovered", 200, "UP", PackagedRuntimeTest::assertAmUp);
                am.verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/oauth2/realms/hmcts/access_token")));
                am.verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/oauth2/access_token")));
                assertTrue(am.findAllUnmatchedRequests().isEmpty(), () -> "Unmatched AM requests: " + am.findAllUnmatchedRequests());
            }
        } finally {
            am.stop();
        }
    }

    private static MappingBuilder tokenForm(String path) {
        return post(urlEqualTo(path))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withFormParam("grant_type", equalTo("password"))
                .withFormParam("scope", equalTo("openid profile roles"))
                .withFormParam("username", equalTo("ci user+&"))
                .withFormParam("password", equalTo("ci pass+&="));
    }

    private static void assertAmUp(JsonNode body) {
        for (String name : AM_COMPONENTS) {
            var component = body.path("components").path(name);
            assertEquals("UP", component.path("status").asString(), name);
            for (String field : List.of("lastChecked", "lastStatusChange")) {
                assertDoesNotThrow(() -> Instant.parse(component.path("details").path(field).asString()), name + ": " + field);
            }
        }
    }

    /** Starts only the published artifact; no test classes or dependencies enter its classpath. */
    private static class Application implements AutoCloseable {
        private final Process process;
        private final int port;
        private final Path log;
        private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        Application(String profile, List<String> extra) throws Exception {
            Files.createDirectories(REPORTS);
            log = REPORTS.resolve(profile + ".log");
            try (var socket = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
                port = socket.getLocalPort();
            }
            Path versionFile = REPORTS.resolve("forgerock-version");
            Files.writeString(versionFile, "ci-forgerock-fixture");
            String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
            var command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                    "-Xmx256m", "-jar", PublishedArtifactTest.executableJar().toString(),
                    "--spring.profiles.active=" + profile, "--server.address=127.0.0.1", "--server.port=" + port,
                    "--azure.keyvault.uri=", "--forgerock.info.path=" + versionFile));
            command.addAll(extra);
            process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        }

        HttpResponse<String> get(String path) throws IOException, InterruptedException {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(Duration.ofSeconds(2)).GET().build(), HttpResponse.BodyHandlers.ofString());
        }

        void health(String label, int code, String status, Consumer<JsonNode> assertions) {
            await().alias(label + "; application log: " + log).pollInterval(Duration.ofMillis(250))
                    .atMost(Duration.ofSeconds(60)).failFast("Application exited; see " + log, () -> !process.isAlive())
                    .untilAsserted(() -> {
                        HttpResponse<String> response;
                        try {
                            response = get("/admin/health");
                        } catch (IOException e) {
                            throw new AssertionError("Health endpoint unavailable; see " + log, e);
                        }
                        Files.writeString(REPORTS.resolve(label + ".json"), response.body());
                        assertEquals(code, response.statusCode(), response.body());
                        var body = JSON.readTree(response.body());
                        assertEquals(status, body.path("status").asString());
                        assertions.accept(body);
                    });
        }

        @Override
        public void close() throws InterruptedException {
            process.destroy();
            try {
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    assertTrue(process.waitFor(5, TimeUnit.SECONDS), "Application did not stop");
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    }
}
