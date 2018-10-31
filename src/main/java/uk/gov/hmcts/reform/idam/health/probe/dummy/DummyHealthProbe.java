package uk.gov.hmcts.reform.idam.health.probe.dummy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatusReport;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.time.LocalDateTime;

@Component
@Slf4j
@Profile("dummy-healthprobe")
public class DummyHealthProbe implements HealthProbe {

    private final DummyHealthStatus dummyHealthStatus;
    private final HealthStatusReport healthReport;

    private final long freshnessInterval;

    public DummyHealthProbe(
            DummyHealthStatus dummyHealthStatus,
            @Value("${dummy.healthprobe.freshness.interval}") Long dummyHealthProbeFreshnessInterval) {
        this.dummyHealthStatus = dummyHealthStatus;
        this.freshnessInterval = dummyHealthProbeFreshnessInterval;

        this.healthReport = new HealthStatusReport();
    }

    @Override
    public boolean isOkay() {
        return healthReport.getStatus() == Status.UP
                && LocalDateTime.now().isBefore(healthReport.getTimestamp().plusSeconds(freshnessInterval));
    }

    @Scheduled(fixedDelayString = "${dummy.healthprobe.check.interval}")
    private void dummyScheduledTask() {
        Status status = dummyHealthStatus.determineStatus();
        healthReport.setStatusName("dummy");
        healthReport.setStatus(status);
        healthReport.setTimestamp(LocalDateTime.now());
    }
}
