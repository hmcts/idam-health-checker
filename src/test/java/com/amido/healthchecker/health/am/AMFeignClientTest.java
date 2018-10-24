package com.amido.healthchecker.health.am;

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

import static com.amido.healthchecker.health.am.AMAccessTokenHealthIndicator.GRANT_TYPE;
import static com.amido.healthchecker.health.am.AMAccessTokenHealthIndicator.SCOPE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class AMFeignClientTest {

    private static final String RESPONSE_BODY = "<body>\n" +
            "\n" +
            "<h1>Server is ALIVE: </h1>\n" +
            "\n" +
            "\n" +
            "</body>";

    private static final String TOKEN_RESPONSE_BODY = "{\n" +
            "\t\"access_token\": \"eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2l\",\n" +
            "\t\"refresh_token\": \"eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2l\",\n" +
            "\t\"scope\": \"acr openid profile roles authorities\",\n" +
            "\t\"id_token\": \"eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjE\",\t\n" +
            "    \"token_type\": \"Bearer\",\n" +
            "\t\"expires_in\": 28799\n" +
            "}";

    private static final String IS_ALIVE_PATH = "/isAlive.jsp";

    private static final String ACCESS_TOKEN_PATH = "/oauth2/access_token?realm=hmcts";

    private AMFeignClient amFeignClient;
    private AMFeignClient amTokenFeignClient;

    private MockClient mockTokenFeignClient;
    private MockClient mockFeignClient;

    @Before
    public void setup() throws IOException {
        mockFeignClient = new MockClient().add(HttpMethod.GET, IS_ALIVE_PATH, 200, RESPONSE_BODY);
        amFeignClient = Feign.builder().client(mockFeignClient).target(new MockTarget<>(AMFeignClient.class));

        mockTokenFeignClient = new MockClient().add(HttpMethod.POST, ACCESS_TOKEN_PATH, 200, TOKEN_RESPONSE_BODY);
        amTokenFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockTokenFeignClient).target(new MockTarget<>(AMFeignClient.class));
    }

    @Test
    public void shouldGetMessageBody() throws IOException {
        Response result = amFeignClient.isAMAlive();
        String bodyMessage = new BufferedReader(new InputStreamReader(result.body().asInputStream())).lines().collect(Collectors.joining("\n"));

        assertThat(bodyMessage, equalTo(RESPONSE_BODY));
        mockFeignClient.verifyOne(HttpMethod.GET, IS_ALIVE_PATH);
        mockFeignClient.verifyStatus();
    }

    @Test
    public void shouldGetAccessToken() throws IOException {
        String host = "some-host";
        String authorization = "aG1jdHM6cGFzc3dvcmQ=";
        String grantType = GRANT_TYPE;
        String username = "tester@test.net";
        String password = "password";
        String scope = SCOPE;

        Response result = amTokenFeignClient.canGenerateAccessToken(host, authorization, grantType, username, password, scope);
        String bodyMessage = new BufferedReader(new InputStreamReader(result.body().asInputStream())).lines().collect(Collectors.joining("\n"));

        assertThat(bodyMessage, equalTo(TOKEN_RESPONSE_BODY));
        mockTokenFeignClient.verifyOne(HttpMethod.POST, ACCESS_TOKEN_PATH);
        mockTokenFeignClient.verifyStatus();
    }
}

