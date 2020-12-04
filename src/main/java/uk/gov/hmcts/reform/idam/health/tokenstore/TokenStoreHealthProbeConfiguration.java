package uk.gov.hmcts.reform.idam.health.tokenstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.ldap.LdapConnectionsHealthProbe;
import uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe;
import uk.gov.hmcts.reform.idam.health.ldap.LdapWorkQueueHealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeFailureHandling;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator;

@Configuration
@Profile("tokenstore")
public class TokenStoreHealthProbeConfiguration {

    @Autowired
    private TokenStoreHealthProbeProperties tokenStoreHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreSearchScheduledHealthProbe(
            TokenStoreSearchHealthProbe tokenStoreSearchHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                tokenStoreSearchHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                tokenStoreHealthProbeProperties.getSearch().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getSearch().getCheckInterval());
    }

    @Bean
    @Profile("!single")
    public ScheduledHealthProbeIndicator tokenStoreReplicationScheduledHealthProbe(
            LdapReplicationHealthProbe ldapReplicationHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapReplicationHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                tokenStoreHealthProbeProperties.getReplication().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getReplication().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreWorkQueueScheduledHealthProbe(
            LdapWorkQueueHealthProbe ldapWorkQueueHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapWorkQueueHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                tokenStoreHealthProbeProperties.getWorkQueue().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getWorkQueue().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreConnectionsHealthProbe(
            LdapConnectionsHealthProbe ldapConnectionsHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapConnectionsHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                tokenStoreHealthProbeProperties.getConnections().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getConnections().getCheckInterval());
    }
}
