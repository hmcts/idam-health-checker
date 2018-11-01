package uk.gov.hmcts.reform.idam.health.probe.am;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatusReport;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Profile("am")
@Slf4j
public class AmHealthProbe implements HealthProbe {

    private final Long amIsAliveFreshnessInterval;
    private final HealthStatusReport amIsAliveStatusReport;
    private final AmIsAliveHealthStatus amIsAliveHealthStatus;

    private final Long amPasswordGrantFreshnessInterval;
    private final HealthStatusReport amPasswordGrantStatusReport;
    private final AmPasswordGrantHealthStatus amPasswordGrantHealthStatus;

    private final HealthStatus[] healthStatus;

    public AmHealthProbe(AmIsAliveHealthStatus amIsAliveHealthStatus,
                         @Value("#{new Long('${am.healthprobe.isalive.freshness.interval}')}") Long amIsAliveFreshnessInterval,
                         AmPasswordGrantHealthStatus amPasswordGrantHealthStatus,
                         @Value("#{new Long('${am.healthprobe.passwordgrant.freshness.interval}')}") Long amPasswordGrantFreshnessInterval) {
        this.amIsAliveFreshnessInterval = amIsAliveFreshnessInterval;
        this.amIsAliveHealthStatus = amIsAliveHealthStatus;
        this.amPasswordGrantFreshnessInterval = amPasswordGrantFreshnessInterval;
        this.amPasswordGrantHealthStatus = amPasswordGrantHealthStatus;

        this.amIsAliveStatusReport = new HealthStatusReport();
        this.amPasswordGrantStatusReport = new HealthStatusReport();

        this.healthStatus = new HealthStatus[2];
        this.healthStatus[1] = amIsAliveHealthStatus;
        this.healthStatus[2] = amPasswordGrantHealthStatus;
    }

    @Override
    public boolean isOkay() {
        return amIsAliveStatusReport.getStatus() == Status.UP
                && isFresh(amIsAliveStatusReport.getTimestamp(), amIsAliveFreshnessInterval)
                && amPasswordGrantStatusReport.getStatus() == Status.UP
                && isFresh(amPasswordGrantStatusReport.getTimestamp(), amPasswordGrantFreshnessInterval);
    }

    @Scheduled(fixedDelayString = "${am.healthprobe.isalive.check.interval}")
    private void amPingTask() {
        Status status = amIsAliveHealthStatus.determineStatus();
        amIsAliveStatusReport.setStatusName("am-ping");
        amIsAliveStatusReport.setStatus(status);
        amIsAliveStatusReport.setTimestamp(LocalDateTime.now());
    }

    @Scheduled(fixedDelayString = "${am.healthprobe.passwordgrant.check.interval}")
    private void amPasswordGrantTask() {
        Status status = amPasswordGrantHealthStatus.determineStatus();
        amPasswordGrantStatusReport.setStatusName("am-password");
        amPasswordGrantStatusReport.setStatus(status);
        amPasswordGrantStatusReport.setTimestamp(LocalDateTime.now());
    }

    private boolean isFresh(LocalDateTime timestamp, Long expiryLimit) {
        return LocalDateTime.now().isBefore(timestamp.plus(expiryLimit, ChronoUnit.MILLIS));
    }

}
