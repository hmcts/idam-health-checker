package com.amido.healthchecker.health;

import feign.Feign;
import feign.Response;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import feign.mock.MockTarget;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private AMFeignClient amFeignClient;
    private MockClient mockFeignClient;



    @Before
    public void setup() throws IOException {
        mockFeignClient = new MockClient().add(HttpMethod.GET, "/isAlive.jsp", 200, RESPONSE_BODY);

        amFeignClient = Feign.builder().client(mockFeignClient).target(new MockTarget<>(AMFeignClient.class));
    }

    @Test
    public void shouldGetMessageBody() throws IOException {

        Response result = amFeignClient.isAMAlive();
        String bodyMessage = new BufferedReader(new InputStreamReader(result.body().asInputStream())).lines().collect(Collectors.joining("\n"));

        assertThat(bodyMessage, equalTo(RESPONSE_BODY));
        mockFeignClient.verifyOne(HttpMethod.GET, "/isAlive.jsp");
        mockFeignClient.verifyStatus();

    }

}

