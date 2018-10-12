package com.amido.healthchecker.health;

import feign.Feign;
import feign.Response;
import feign.form.FormEncoder;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import feign.mock.MockTarget;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.amido.healthchecker.health.AccessTokenHealthIndicator.GRANT_TYPE;
import static com.amido.healthchecker.health.AccessTokenHealthIndicator.SCOPE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class AMFeignClientTest {

    private static final String RESPONSE_BODY = "<body>\n" +
            "\n" +
            "<h1>Server is ALIVE: </h1>\n" +
            "\n" +
            "\n" +
            "</body>";

    private static final String TOKEN_RESPONSE_BODY = "TODO";

    private AMFeignClient amFeignClient;
    private AMFeignClient amTokenFeignClient;

    private MockClient mockTokenFeignClient;
    private MockClient mockFeignClient;

    @Before
    public void setup() throws IOException {
        mockFeignClient = new MockClient().add(HttpMethod.GET, "/isAlive.jsp", 200, RESPONSE_BODY);
        amFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockFeignClient).target(new MockTarget<>(AMFeignClient.class));

        mockTokenFeignClient = new MockClient().add(HttpMethod.POST, "/oauth2/hmcts/access_token", 200, TOKEN_RESPONSE_BODY);
        amTokenFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockTokenFeignClient).target(new MockTarget<>(AMFeignClient.class));
    }

    @Test
    public void shouldGetMessageBody() throws IOException {
        Response result = amFeignClient.isAMAlive();
        String bodyMessage = new BufferedReader(new InputStreamReader(result.body().asInputStream())).lines().collect(Collectors.joining("\n"));

        assertThat(bodyMessage, equalTo(RESPONSE_BODY));
        mockFeignClient.verifyOne(HttpMethod.GET, "/isAlive.jsp");
        mockFeignClient.verifyStatus();
    }

    @Test
    public void shouldGetAccessToken() throws IOException {
        String authorization = "aG1jdHM6cGFzc3dvcmQ=";
        String grantType = GRANT_TYPE;
        String username = "tester@test.net";
        String password = "password";
        String scope = SCOPE;

        Response result = amTokenFeignClient.canGenerateAccessToken(authorization, grantType, username, password, scope);
        String bodyMessage = new BufferedReader(new InputStreamReader(result.body().asInputStream())).lines().collect(Collectors.joining("\n"));

        assertThat(bodyMessage, equalTo(TOKEN_RESPONSE_BODY));
        mockTokenFeignClient.verifyOne(HttpMethod.POST, "/oauth2/hmcts/access_token");
        mockTokenFeignClient.verifyStatus();
    }
}

