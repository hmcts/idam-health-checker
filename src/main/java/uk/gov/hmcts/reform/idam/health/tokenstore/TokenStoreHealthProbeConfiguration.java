package uk.gov.hmcts.reform.idam.health.tokenstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeFailureHandling;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator;

@Configuration
@Profile("tokenstore")
public class TokenStoreHealthProbeConfiguration {

    @Autowired
    private TokenStoreHealthProbeProperties tokenStoreHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreAliveScheduledHealthProbe(
            TokenStoreAliveHealthProbe tokenStoreAliveHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                tokenStoreAliveHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                tokenStoreHealthProbeProperties.getAlive().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getAlive().getCheckInterval());
    }

}
