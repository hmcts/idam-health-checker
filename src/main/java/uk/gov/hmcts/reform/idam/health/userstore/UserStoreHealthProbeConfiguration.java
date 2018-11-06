package uk.gov.hmcts.reform.idam.health.userstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeExecutor;

@Configuration
@Profile("userstore")
public class UserStoreHealthProbeConfiguration {

    @Autowired
    private UserStoreHealthProbeProperties userStoreHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeExecutor userStoreAuthenticationScheduledHealthProbe(
            UserStoreAuthenticationHealthProbe userStoreAuthenticationHealthProbe) {
        return new ScheduledHealthProbeExecutor(
                userStoreAuthenticationHealthProbe,
                taskScheduler,
                userStoreHealthProbeProperties.getAuthentication().getFreshnessInterval(),
                userStoreHealthProbeProperties.getAuthentication().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeExecutor userStoreReplicationScheduledHealthProbe(
            LdapReplicationHealthProbe ldapReplicationHealthProbe) {
        return new ScheduledHealthProbeExecutor(
                ldapReplicationHealthProbe,
                taskScheduler,
                userStoreHealthProbeProperties.getReplication().getFreshnessInterval(),
                userStoreHealthProbeProperties.getReplication().getCheckInterval());
    }
}
