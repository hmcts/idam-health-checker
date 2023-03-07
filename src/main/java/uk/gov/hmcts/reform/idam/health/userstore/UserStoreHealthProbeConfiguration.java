package uk.gov.hmcts.reform.idam.health.userstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeFailureHandling;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator;

@Configuration
@Profile("userstore")
public class UserStoreHealthProbeConfiguration {

    @Autowired
    private UserStoreHealthProbeProperties userStoreHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator userStoreAliveScheduledHealthProbe(
            UserStoreAliveHealthProbe userStoreAliveHealthProbe ) {
        return new ScheduledHealthProbeIndicator(
                userStoreAliveHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                userStoreHealthProbeProperties.getAlive().getFreshnessInterval(),
                userStoreHealthProbeProperties.getAlive().getCheckInterval());
    }

}
