package uk.gov.hmcts.reform.idam.health.probe.tokenstore;

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
@Profile("tokenstore")
@Slf4j
public class TokenStoreHealthProbe implements HealthProbe {

    private final Long tokenStoreSearchFreshnessInterval;
    private final HealthStatusReport tokenStoreSearchReport;
    private final TokenStoreSearchHealthStatus tokenStoreSearchHealthStatus;

    public TokenStoreHealthProbe(
            TokenStoreSearchHealthStatus tokenStoreSearchHealthStatus,
            @Value("#{new Long('${tokenstore.healthprobe.search.freshness.interval}')}") Long userStoreSearchFreshnessInterval) {
        this.tokenStoreSearchFreshnessInterval = userStoreSearchFreshnessInterval;
        this.tokenStoreSearchHealthStatus = tokenStoreSearchHealthStatus;

        this.tokenStoreSearchReport = new HealthStatusReport();
    }

    @Override
    public boolean isOkay() {
        return tokenStoreSearchReport.getStatus() == Status.UP
                && LocalDateTime.now().isBefore(tokenStoreSearchReport.getTimestamp().plus(tokenStoreSearchFreshnessInterval, ChronoUnit.MILLIS));
    }


    @Scheduled(fixedDelayString = "${tokenstore.healthprobe.search.check.interval}")
    private void tokenStoreSearchTask() {
        Status status = tokenStoreSearchHealthStatus.determineStatus();
        tokenStoreSearchReport.setStatusName("tokenstore-search");
        tokenStoreSearchReport.setStatus(status);
        tokenStoreSearchReport.setTimestamp(LocalDateTime.now());
    }
}
