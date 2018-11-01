package uk.gov.hmcts.reform.idam.health.probe.userstore;

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

@Component
@Profile("userstore")
@Slf4j
public class UserStoreHealthProbe implements HealthProbe {

    private final UserStoreHealthProbeProperties healthProbeProperties;
    private final HealthStatusReport userStoreAuthenticationReport;
    private final UserStoreAuthenticationHealthStatus userStoreAuthenticationHealthStatus;

    public UserStoreHealthProbe(
            UserStoreAuthenticationHealthStatus userStoreAuthenticationHealthStatus,
            UserStoreHealthProbeProperties healthProbeProperties) {
        this.healthProbeProperties = healthProbeProperties;
        this.userStoreAuthenticationHealthStatus = userStoreAuthenticationHealthStatus;

        this.userStoreAuthenticationReport = new HealthStatusReport();
    }

    @Override
    public boolean isOkay() {
        return userStoreAuthenticationReport.getStatus() == Status.UP
                && LocalDateTime.now().isBefore(
                        userStoreAuthenticationReport.getTimestamp().plus(
                                healthProbeProperties.getAuthentication().getFreshnessInterval(), ChronoUnit.MILLIS));
    }

    @Scheduled(fixedDelayString = "#{@userStoreHealthProbeProperties.authentication.checkInterval}")
    private void userStoreAuthenticationTask() {
        Status status = userStoreAuthenticationHealthStatus.determineStatus();
        userStoreAuthenticationReport.setStatusName("userstore-authentication");
        userStoreAuthenticationReport.setStatus(status);
        userStoreAuthenticationReport.setTimestamp(LocalDateTime.now());
    }
}
