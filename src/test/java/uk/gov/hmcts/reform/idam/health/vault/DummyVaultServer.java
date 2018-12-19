package uk.gov.hmcts.reform.idam.health.vault;

import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Server to produce responses as per Azure Vault.
 */
public class DummyVaultServer {

    protected static final Map<String, String> VAULT_PROPERTIES = ImmutableMap.of(
            "test-owner-username", "phil.space",
            "test-owner-password", "PasswOrd",
            "web-admin-client-secret", "secret",
            "BINDPASSWD", "ABCDE",
            "appinsights-instrumentationkey", "ABCDE12345");

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

    final static String INVALID_TOKEN_RESPONSE = "{\n"+
            "\t\"not_access_token\": \"eyJ0eXAiOiJKV1...hQ5J4_hoQ\"\n"+
            "}";

    final static String SECRET_RESPONSE_TEMPLATE = "{\"value\":\"%s\",\n" +
            "\t\"id\":\"https://test.vault.azure.net/secrets/%s/5f5b24471cca47f99cdd3204d41372d2\",\n" +
            "\t\"attributes\":{\"enabled\":true,\"created\":1541609008,\"updated\":1541609008,\"recoveryLevel\":\"Purgeable\"},\n" +
            "\t\"tags\":{\"file-encoding\":\"utf-8\"}}";

    public DummyVaultServer(final int port) {

        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/metadata/identity/oauth2", new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    System.out.println("Returning token for " + exchange.getRequestURI());
                    byte[] response = TOKEN_RESPONSE.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                }
            });
            httpServer.createContext("/test", new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    System.out.println("Returning secret for " + exchange.getRequestURI());
                    String path = exchange.getRequestURI().getPath();

                    final String responseString = generateResponse(path);

                    byte[] response = responseString.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                }
            });

            httpServer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateResponse(String path) {
        for (String key : VAULT_PROPERTIES.keySet()) {
            if (path.contains(key)) {
                return String.format(SECRET_RESPONSE_TEMPLATE, VAULT_PROPERTIES.get(key), key);
            }
        }

        return String.format(SECRET_RESPONSE_TEMPLATE, "--", "--");
    }

    public static void main(String[] args) {
        DummyVaultServer server = new DummyVaultServer(9999);
    }
}
