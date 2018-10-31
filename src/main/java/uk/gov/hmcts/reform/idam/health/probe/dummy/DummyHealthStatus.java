package uk.gov.hmcts.reform.idam.health.probe.dummy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

@Component
@Slf4j
public class DummyHealthStatus implements HealthStatus {

    @Override
    public Status determineStatus() {
        if (System.nanoTime() % 2 == 0) {
            return Status.DOWN;
        }
        log.info("DUMMY HEALTH PROBE JUST RAN");
        return Status.UP;
    }
}
