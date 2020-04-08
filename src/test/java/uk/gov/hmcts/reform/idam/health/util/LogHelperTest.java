package uk.gov.hmcts.reform.idam.health.util;

import ch.qos.logback.classic.Logger;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LogHelperTest {

    @Test
    public void getLogger() {
        final Logger logger = LogHelper.getLogger(LogHelperTest.class);
        assertNotNull("The logger should not be null.", logger);
    }
}