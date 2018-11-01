package uk.gov.hmcts.reform.idam.health.probe.am;

import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.io.IOException;

@Component
@Profile("am")
@Slf4j
public class AmIsAliveHealthStatus implements HealthStatus {

    private static final String ALIVE = "ALIVE";

    private final AmProvider amProvider;

    public AmIsAliveHealthStatus(AmProvider amProvider) {
        this.amProvider = amProvider;
    }

    @Override
    public Status determineStatus() {
        try {
            String body = amProvider.isAlive();
            if (StringUtils.contains(body, ALIVE)) {
                log.info("is alive success");
                return Status.UP;
            }
        } catch (Exception e) {
            log.error("AM isAlive: " + e.getMessage());
        }
        return Status.DOWN;
    }

}
