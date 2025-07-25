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
    public ScheduledHealthProbeIndicator amReadyScheduledHealthProbe(AMReadyHealthProbe amReadyHealthProbe) {
        return new ScheduledHealthProbeIndicator(amReadyHealthProbe,
                                                 HealthProbeFailureHandling.MARK_AS_DOWN,
                                                 taskScheduler,
                                                 amHealthProbeProperties.getReady().getFreshnessInterval(),
                                                 amHealthProbeProperties.getReady().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator amPasswordGrantScheduledHealthProbe(AmPasswordGrantHealthProbe amPasswordGrantHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                amPasswordGrantHealthProbe,
                HealthProbeFailureHandling.IGNORE_ONCE_READY,
                taskScheduler,
                amHealthProbeProperties.getPasswordGrant().getFreshnessInterval(),
                amHealthProbeProperties.getPasswordGrant().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator amRootPasswordGrantScheduledHealthProbe(AmRootPasswordGrantHealthProbe amRootPasswordGrantHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                amRootPasswordGrantHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                amHealthProbeProperties.getRootPasswordGrant().getFreshnessInterval(),
                amHealthProbeProperties.getRootPasswordGrant().getCheckInterval());
    }
}
