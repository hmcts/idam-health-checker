package com.amido.healthchecker.health.idm;

import feign.Feign;
import feign.form.FormEncoder;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import feign.mock.MockTarget;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import static com.amido.healthchecker.health.idm.ServerStatus.SERVER_IS_READY;
import static com.amido.healthchecker.health.idm.ServerStatus.SERVER_NOT_READY;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class IDMHealthIndicatorTest {

    private static final String RESPONSE_BODY = "{\n" +
            "    \"_id\": \"\",\n" +
            "    \"shortDesc\": \"OpenIDM ready\",\n" +
            "    \"state\": \"ACTIVE_READY\"\n" +
            "}";

    private IDMFeignClient idmFeignClient;
    private MockClient mockHappyFeignClient;
    private MockClient mockUnauthorizedFeignClient;
    private MockClient mockErrorFeignClient;
    private IDMHealthIndicator idmHealthIndicator;

    private static final String TOKEN_PATH = "/info/ping";

    @Before
    public void setup() {
        mockHappyFeignClient = new MockClient().add(HttpMethod.GET, TOKEN_PATH, 200, RESPONSE_BODY);
        mockUnauthorizedFeignClient = new MockClient().add(HttpMethod.GET, TOKEN_PATH, 401, "");
        mockErrorFeignClient = new MockClient().add(HttpMethod.GET, TOKEN_PATH, 500, "");
    }

    @Test
    public void checkIDMReady() {
        //given
        idmFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockHappyFeignClient).target(new MockTarget<>(IDMFeignClient.class));
        idmHealthIndicator = new IDMHealthIndicator(idmFeignClient);

        //when
        Health healthStatus = idmHealthIndicator.health();

        //then
        mockHappyFeignClient.verifyOne(HttpMethod.GET, TOKEN_PATH);
        assertThat(healthStatus.getStatus().getCode(), equalTo("UP"));
        assertThat(healthStatus.getDetails().get("message"), equalTo(SERVER_IS_READY));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(null));
    }

    @Test
    public void checkIDMNotReady() {
        //given
        idmFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockUnauthorizedFeignClient).target(new MockTarget<>(IDMFeignClient.class));
        idmHealthIndicator = new IDMHealthIndicator(idmFeignClient);

        //when
        Health healthStatus = idmHealthIndicator.health();

        //then
        mockUnauthorizedFeignClient.verifyOne(HttpMethod.GET, TOKEN_PATH);
        assertThat(healthStatus.getStatus().getCode(), equalTo("DOWN"));
        assertThat(healthStatus.getDetails().get("message"), equalTo(SERVER_NOT_READY));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(500));
    }

    @Test
    public void checkInternalServerErrorResponse() {
        //given
        idmFeignClient = Feign.builder().encoder(new FormEncoder()).client(mockErrorFeignClient).target(new MockTarget<>(IDMFeignClient.class));
        idmHealthIndicator = new IDMHealthIndicator(idmFeignClient);

        //when
        Health healthStatus = idmHealthIndicator.health();

        //then
        mockErrorFeignClient.verifyOne(HttpMethod.GET, TOKEN_PATH);
        assertThat(healthStatus.getStatus().getCode(), equalTo("DOWN"));
        assertThat(healthStatus.getDetails().get("message"), equalTo(SERVER_NOT_READY));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(500));
    }

}