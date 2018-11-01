package uk.gov.hmcts.reform.idam.health.probe.tokenstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Profile("tokenstore")
@Slf4j
public class TokenStoreHealthProbe implements HealthProbe {

    private final TokenStoreHealthProbeProperties healthProbeProperties;
    private final HealthStatusReport tokenStoreSearchReport;
    private final TokenStoreSearchHealthStatus tokenStoreSearchHealthStatus;

    public TokenStoreHealthProbe(
            TokenStoreSearchHealthStatus tokenStoreSearchHealthStatus,
            TokenStoreHealthProbeProperties healthProbeProperties) {
        this.healthProbeProperties = healthProbeProperties;
        this.tokenStoreSearchHealthStatus = tokenStoreSearchHealthStatus;

        this.tokenStoreSearchReport = new HealthStatusReport();
    }

    @Override
    public boolean isOkay() {
        return tokenStoreSearchReport.getStatus() == Status.UP
                && LocalDateTime.now().isBefore(
                        tokenStoreSearchReport.getTimestamp().plus(
                                healthProbeProperties.getSearch().getFreshnessInterval(), ChronoUnit.MILLIS));
    }


    @Scheduled(fixedDelayString = "#{@tokenStoreHealthProbeProperties.search.checkInterval}")
    private void tokenStoreSearchTask() {
        Status status = tokenStoreSearchHealthStatus.determineStatus();
        tokenStoreSearchReport.setStatusName("tokenstore-search");
        tokenStoreSearchReport.setStatus(status);
        tokenStoreSearchReport.setTimestamp(LocalDateTime.now());
    }
}
