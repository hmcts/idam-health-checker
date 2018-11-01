package uk.gov.hmcts.reform.idam.health.probe.idm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatusReport;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Profile("idm")
@Slf4j
public class IdmHealthProbe implements HealthProbe {

    private final IdmHealthProbeProperties healthProbeProperties;
    private final HealthStatusReport idmPingStatusReport;
    private final IdmPingHealthStatus idmPingHealthStatus;

    public IdmHealthProbe(
            IdmPingHealthStatus idmPingHealthStatus,
            IdmHealthProbeProperties healthProbeProperties) {

        this.healthProbeProperties = healthProbeProperties;
        this.idmPingHealthStatus = idmPingHealthStatus;

        this.idmPingStatusReport = new HealthStatusReport();
    }

    @Override
    public boolean isOkay() {
        return idmPingStatusReport.getStatus() == Status.UP
                && LocalDateTime.now().isBefore(
                        idmPingStatusReport.getTimestamp()
                                .plus(healthProbeProperties.getPing().getFreshnessInterval(), ChronoUnit.MILLIS));
    }

    @Scheduled(fixedDelayString = "#{@idmHealthProbeProperties.ping.checkInterval}")
    private void idmPingTask() {
        Status status = idmPingHealthStatus.determineStatus();
        idmPingStatusReport.setStatusName("idm-ping");
        idmPingStatusReport.setStatus(status);
        idmPingStatusReport.setTimestamp(LocalDateTime.now());
    }
}
