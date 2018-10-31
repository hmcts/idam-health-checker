package uk.gov.hmcts.reform.idam.health.probe.idm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatusReport;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Component
@Profile("idm")
@Slf4j
public class IdmHealthProbe implements HealthProbe {

    private final Long idmPingFreshnessInterval;
    private final HealthStatusReport idmPingStatusReport;
    private final IdmPingHealthStatus idmPingHealthStatus;

    public IdmHealthProbe(
            IdmPingHealthStatus idmPingHealthStatus,
            @Value("#{new Long('${idm.healthprobe.ping.freshness.interval}')}") Long idmPingFreshnessInterval) {

        this.idmPingFreshnessInterval = idmPingFreshnessInterval;
        this.idmPingHealthStatus = idmPingHealthStatus;

        this.idmPingStatusReport = new HealthStatusReport();
    }

    @Override
    public boolean isOkay() {
        return idmPingStatusReport.getStatus() == Status.UP
                && LocalDateTime.now().isBefore(idmPingStatusReport.getTimestamp().plus(idmPingFreshnessInterval, ChronoUnit.MILLIS));
    }

    @Scheduled(fixedDelayString = "${idm.healthprobe.ping.check.interval}")
    private void idmPingTask() {
        Status status = idmPingHealthStatus.determineStatus();
        idmPingStatusReport.setStatusName("idm-ping");
        idmPingStatusReport.setStatus(status);
        idmPingStatusReport.setTimestamp(LocalDateTime.now());
    }
}
