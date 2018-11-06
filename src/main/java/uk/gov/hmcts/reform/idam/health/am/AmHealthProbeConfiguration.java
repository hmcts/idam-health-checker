package uk.gov.hmcts.reform.idam.health.am;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeExecutor;

@Configuration
@Profile("am")
public class AmHealthProbeConfiguration {

    @Autowired
    private AmHealthProbeProperties amHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeExecutor amIsAliveScheduledHealthProbe(AmIsAliveHealthProbe amIsAliveHealthProbe) {
        return new ScheduledHealthProbeExecutor(
                amIsAliveHealthProbe,
                taskScheduler,
                amHealthProbeProperties.getIsAlive().getFreshnessInterval(),
                amHealthProbeProperties.getIsAlive().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeExecutor amPasswordGrantScheduledHealthProbe(AmPasswordGrantHealthProbe amPasswordGrantHealthProbe) {
        return new ScheduledHealthProbeExecutor(
                amPasswordGrantHealthProbe,
                taskScheduler,
                amHealthProbeProperties.getPasswordGrant().getFreshnessInterval(),
                amHealthProbeProperties.getPasswordGrant().getCheckInterval());
    }
}
