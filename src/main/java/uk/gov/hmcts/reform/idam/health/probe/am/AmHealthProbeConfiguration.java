package uk.gov.hmcts.reform.idam.health.probe.am;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbe;

@Configuration
@Profile("am")
public class AmHealthProbeConfiguration {

    @Autowired
    private AmHealthProbeProperties amHealthProbeProperties;

    @Bean
    public ScheduledHealthProbe amIsAliveHealthProbe(AmIsAliveHealthProbe amIsAliveHealthProbe) {
        return new ScheduledHealthProbe(
                amIsAliveHealthProbe,
                amHealthProbeProperties.getIsAlive().getFreshnessInterval(),
                amHealthProbeProperties.getIsAlive().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbe amPasswordGrantHealthProbe(AmPasswordGrantHealthProbe amPasswordGrantHealthProbe) {
        return new ScheduledHealthProbe(
                amPasswordGrantHealthProbe,
                amHealthProbeProperties.getPasswordGrant().getFreshnessInterval(),
                amHealthProbeProperties.getPasswordGrant().getCheckInterval());
    }
}
