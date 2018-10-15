package com.amido.healthchecker.health;

import feign.Feign;
import feign.form.FormEncoder;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import feign.mock.MockTarget;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AmTokenHealthIndicatorTest {

    private static final String TOKEN_RESPONSE_BODY = "{\n" +
            "\t\"access_token\": \"eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2l\",\n" +
            "\t\"refresh_token\": \"eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2l\",\n" +
            "\t\"scope\": \"acr openid profile roles authorities\",\n" +
            "\t\"id_token\": \"eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjE\",\t\n" +
            "    \"token_type\": \"Bearer\",\n" +
            "\t\"expires_in\": 28799\n" +
            "}";

    private AMFeignClient amFeignClient;
    private MockClient mockHappyFeignClient;
    private MockClient mockUnauthorizedFeignClient;
    private MockClient mockErrorFeignClient;
    private AccessTokenHealthIndicator accessTokenHealthIndicator;

    private static final String TOKEN_PATH = "/oauth2/hmcts/access_token";

    @Before
    public void setup() {
        mockHappyFeignClient = new MockClient().add(HttpMethod.POST, TOKEN_PATH, 200, TOKEN_RESPONSE_BODY);
        mockUnauthorizedFeignClient = new MockClient().add(HttpMethod.POST, TOKEN_PATH, 401, TOKEN_RESPONSE_BODY);
        mockErrorFeignClient = new MockClient().add(HttpMethod.POST, TOKEN_PATH, 500, TOKEN_RESPONSE_BODY);
    }

    @Test
    public void checkAccessTokenIsPresent() {
        //given
        amFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockHappyFeignClient).target(new MockTarget<>(AMFeignClient.class));
        accessTokenHealthIndicator = new AccessTokenHealthIndicator(amFeignClient);

        //when
        Health healthStatus = accessTokenHealthIndicator.health();

        //then
        mockHappyFeignClient.verifyOne(HttpMethod.POST, TOKEN_PATH);
        assertThat(healthStatus.getStatus().getCode(), equalTo("UP"));
        assertThat(healthStatus.getDetails().get("message"), equalTo("Server returned access_token"));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(null));
    }

    @Test
    public void checkUnauthorizedResponse() {
        //given
        amFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockUnauthorizedFeignClient).target(new MockTarget<>(AMFeignClient.class));
        accessTokenHealthIndicator = new AccessTokenHealthIndicator(amFeignClient);

        //when
        Health healthStatus = accessTokenHealthIndicator.health();

        //then
        mockUnauthorizedFeignClient.verifyOne(HttpMethod.POST, TOKEN_PATH);
        assertThat(healthStatus.getStatus().getCode(), equalTo("DOWN"));
        assertThat(healthStatus.getDetails().get("message"), equalTo("Unauthorized"));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(401));
    }

    @Test
    public void checkInternalServerErrorResponse() {
        //given
        amFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockErrorFeignClient).target(new MockTarget<>(AMFeignClient.class));
        accessTokenHealthIndicator = new AccessTokenHealthIndicator(amFeignClient);

        //when
        Health healthStatus = accessTokenHealthIndicator.health();

        //then
        mockErrorFeignClient.verifyOne(HttpMethod.POST, TOKEN_PATH);
        assertThat(healthStatus.getStatus().getCode(), equalTo("DOWN"));
        assertThat(healthStatus.getDetails().get("message"), equalTo("Internal Server Error"));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(500));
    }

}