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
    public ScheduledHealthProbeIndicator amLiveScheduledHealthProbe(AmLiveHealthProbe amLiveHealthProbe) {
        return new ScheduledHealthProbeIndicator(amLiveHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                amHealthProbeProperties.getLive().getFreshnessInterval(),
                amHealthProbeProperties.getLive().getCheckInterval());
    }

    @Bean
    @Profile("check-ready")
    public ScheduledHealthProbeIndicator amPasswordGrantScheduledHealthProbe(AmPasswordGrantHealthProbe amPasswordGrantHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                amPasswordGrantHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                amHealthProbeProperties.getPasswordGrant().getFreshnessInterval(),
                amHealthProbeProperties.getPasswordGrant().getCheckInterval());
    }
}
