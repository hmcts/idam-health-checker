package uk.gov.hmcts.reform.idam.health.idm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbe;

@Configuration
@Profile("idm")
public class IdmHealthProbeConfiguration {

    @Autowired
    private IdmHealthProbeProperties idmHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbe idmPingScheduledHealthProbe(IdmPingHealthProbe idmPingHealthProbe) {
        return new ScheduledHealthProbe(
                idmPingHealthProbe,
                taskScheduler,
                idmHealthProbeProperties.getPing().getFreshnessInterval(),
                idmHealthProbeProperties.getPing().getCheckInterval());
    }
}
