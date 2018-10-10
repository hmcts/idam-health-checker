package com.amido.healthchecker.health;

import feign.Feign;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import feign.mock.MockTarget;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AmHealthIndicatorTest {

    private static final String HAPPY_RESPONSE_BODY = "<body>\n" +
            "\n" +
            "<h1>Server is ALIVE: </h1>\n" +
            "\n" +
            "\n" +
            "</body>";
    private static final String DOWN_RESPONSE_BODY = "<body>\n" +
            "\n" +
            "<h1>Server is DOWN </h1>\n" +
            "\n" +
            "\n" +
            "</body>";

    private AMFeignClient amFeignClient;
    private MockClient mockHappyFeignClient;
    private MockClient mockDownFeignClient;
    private MockClient mockErrorFeignClient;
    private AmHealthIndicator amHealthIndicator;


    @Before
    public void setup(){
        mockHappyFeignClient = new MockClient().add(HttpMethod.GET, "/isAlive.jsp", 200, HAPPY_RESPONSE_BODY);
        mockDownFeignClient = new MockClient().add(HttpMethod.GET, "/isAlive.jsp", 503, DOWN_RESPONSE_BODY);
        mockErrorFeignClient = new MockClient().add(HttpMethod.GET, "/isAlive.jsp", 500, "unknown response");
    }

    @Test
    public void checkAMIsAlive(){

        //given
        amFeignClient = Feign.builder().client(mockHappyFeignClient).target(new MockTarget<>(AMFeignClient.class));
        amHealthIndicator = new AmHealthIndicator(amFeignClient);

        //when
        Health healthStatus = amHealthIndicator.health();

        //then
        mockHappyFeignClient.verifyOne(HttpMethod.GET, "/isAlive.jsp");
        assertThat(healthStatus.getStatus().getCode(), equalTo("UP"));
        assertThat(healthStatus.getDetails().get("message"), equalTo("Server is ALIVE"));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(0));

    }


    @Test
    public void checkAMIsDown(){

        //given
        amFeignClient = Feign.builder().client(mockDownFeignClient).target(new MockTarget<>(AMFeignClient.class));
        amHealthIndicator = new AmHealthIndicator(amFeignClient);

        //when
        Health healthStatus = amHealthIndicator.health();

        //then
        mockDownFeignClient.verifyOne(HttpMethod.GET, "/isAlive.jsp");
        assertThat(healthStatus.getStatus().getCode(), equalTo("DOWN"));
        assertThat(healthStatus.getDetails().get("message"), equalTo("Server is DOWN"));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(503));

    }

    @Test
    public void checkAMHasInternalServerError(){

        //given
        amFeignClient = Feign.builder().client(mockErrorFeignClient).target(new MockTarget<>(AMFeignClient.class));
        amHealthIndicator = new AmHealthIndicator(amFeignClient);

        //when
        Health healthStatus = amHealthIndicator.health();

        //then
        mockErrorFeignClient.verifyOne(HttpMethod.GET, "/isAlive.jsp");
        assertThat(healthStatus.getStatus().getCode(), equalTo("DOWN"));
        assertThat(healthStatus.getDetails().get("message"), equalTo("Internal Server Error"));
        assertThat(healthStatus.getDetails().get("errorCode"), equalTo(500));

    }

}