package uk.gov.hmcts.reform.idam.health.userstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe;
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
    public ScheduledHealthProbeIndicator userStoreAuthenticationScheduledHealthProbe(
            UserStoreAuthenticationHealthProbe userStoreAuthenticationHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                userStoreAuthenticationHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                userStoreHealthProbeProperties.getAuthentication().getFreshnessInterval(),
                userStoreHealthProbeProperties.getAuthentication().getCheckInterval());
    }

    @Bean
    @Profile("!single")
    public ScheduledHealthProbeIndicator userStoreReplicationScheduledHealthProbe(
            LdapReplicationHealthProbe ldapReplicationHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapReplicationHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                userStoreHealthProbeProperties.getReplication().getFreshnessInterval(),
                userStoreHealthProbeProperties.getReplication().getCheckInterval());
    }
}
