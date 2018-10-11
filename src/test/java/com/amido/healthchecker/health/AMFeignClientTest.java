package com.amido.healthchecker.health;

import feign.Feign;
import feign.Response;
import feign.form.FormEncoder;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import feign.mock.MockTarget;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    private MockClient mockTokenFeignClient;
    private MockClient mockFeignClient;

    @Before
    public void setup() throws IOException {
        mockFeignClient = new MockClient().add(HttpMethod.GET, "/isAlive.jsp", 200, RESPONSE_BODY);
        mockTokenFeignClient = new MockClient().add(HttpMethod.POST, "/oauth2/hmcts/access_token", 200, TOKEN_RESPONSE_BODY);

        amFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockFeignClient).target(new MockTarget<>(AMFeignClient.class));
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
    @Ignore
    public void shouldGetAccessToken() throws IOException {
        String authorization = "Basic aG1jdHM6cGFzc3dvcmQ=";
        String grantType = "password";
        String username = "idamOwner@hmcts.net";
        String password = "Passw1rd";
        String scope = "openid profile authorities acr roles";

        Response result = amFeignClient.canGenerateAccessToken(authorization, grantType, username, password, scope);
        String bodyMessage = new BufferedReader(new InputStreamReader(result.body().asInputStream())).lines().collect(Collectors.joining("\n"));

        assertThat(bodyMessage, equalTo(TOKEN_RESPONSE_BODY));
        mockTokenFeignClient.verifyOne(HttpMethod.POST, "/oauth2/hmcts/access_token").headers().containsKey("Authorization");
        mockTokenFeignClient.verifyStatus();
    }
}

