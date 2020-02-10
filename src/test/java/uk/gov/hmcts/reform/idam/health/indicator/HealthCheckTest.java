package uk.gov.hmcts.reform.idam.health.indicator;

import org.junit.Test;
import org.springframework.boot.actuate.health.Status;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class HealthCheckTest {

    @Test
    public void anEmptyList() throws Exception {
        assertEquals(Status.UP, new HealthCheck(Collections.emptyList()).health().getStatus());
    }
}
