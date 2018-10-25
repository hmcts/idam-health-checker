package uk.gov.hmcts.reform.idam.healthchecker.health.idm;

import feign.Feign;
import feign.Response;
import feign.codec.Decoder;
import feign.mock.HttpMethod;
import feign.mock.MockClient;
import feign.mock.MockTarget;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class IDMFeignClientTest {

    private static final String RESPONSE_BODY = "{\n" +
            "    \"_id\": \"\",\n" +
            "    \"shortDesc\": \"OpenIDM ready\",\n" +
            "    \"state\": \"ACTIVE_READY\"\n" +
            "}";

    private IDMFeignClient idmFeignClient;

    private MockClient mockFeignClient;

    private static final String PING_PATH = "/info/ping";

    @Before
    public void setup() throws IOException {
        mockFeignClient = new MockClient().add(HttpMethod.GET, PING_PATH, 200, RESPONSE_BODY);
        idmFeignClient = Feign.builder().client(mockFeignClient).target(new MockTarget<>(IDMFeignClient.class));
    }

    @Test
    public void shouldGetMessageBody() throws IOException {
        final String authorization = "somethingsomething==";

        Response result = idmFeignClient.pingIdm(authorization);

        final Decoder decoder = new Decoder.Default();
        final String bodyMessage = (String)decoder.decode(result, String.class);

        assertThat(bodyMessage, equalTo(RESPONSE_BODY));
        mockFeignClient.verifyOne(HttpMethod.GET, PING_PATH);
        mockFeignClient.verifyStatus();
    }
}

