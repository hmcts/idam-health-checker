package uk.gov.hmcts.reform.idam.health.am;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeFailureHandling;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator;

@Configuration
@Profile("am")
public class AmHealthProbeConfiguration {

    @Autowired
    private AmHealthProbeProperties amHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator amIsAliveScheduledHealthProbe(AmIsAliveHealthProbe amIsAliveHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                amIsAliveHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                amHealthProbeProperties.getIsAlive().getFreshnessInterval(),
                amHealthProbeProperties.getIsAlive().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator amPasswordGrantScheduledHealthProbe(AmPasswordGrantHealthProbe amPasswordGrantHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                amPasswordGrantHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                amHealthProbeProperties.getPasswordGrant().getFreshnessInterval(),
                amHealthProbeProperties.getPasswordGrant().getCheckInterval());
    }
}
