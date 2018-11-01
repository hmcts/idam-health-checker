package uk.gov.hmcts.reform.idam.health.userstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbe;

@Configuration
@Profile("userstore")
public class UserStoreHealthProbeConfiguration {

    @Autowired
    private UserStoreHealthProbeProperties userStoreHealthProbeProperties;

    @Bean
    public ScheduledHealthProbe userStoreAuthenticationHealthProbe(
            UserStoreAuthenticationHealthProbe userStoreAuthenticationHealthProbe) {
        return new ScheduledHealthProbe(
                userStoreAuthenticationHealthProbe,
                userStoreHealthProbeProperties.getAuthentication().getFreshnessInterval(),
                userStoreHealthProbeProperties.getAuthentication().getCheckInterval());
    }
}
