package uk.gov.hmcts.reform.idam.health.idm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbe;

@Configuration
@Profile("idm")
public class IdmHealthProbeConfiguration {

    @Autowired
    private IdmHealthProbeProperties idmHealthProbeProperties;

    public ScheduledHealthProbe idmPingScheduledHealthProbe(IdmPingHealthProbe idmPingHealthProbe) {
        return new ScheduledHealthProbe(
                idmPingHealthProbe,
                idmHealthProbeProperties.getPing().getFreshnessInterval(),
                idmHealthProbeProperties.getPing().getCheckInterval());
    }
}
