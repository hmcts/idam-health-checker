package uk.gov.hmcts.reform.idam.health.tokenstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.ldap.LdapConnectionsHealthProbe;
import uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe;
import uk.gov.hmcts.reform.idam.health.ldap.LdapWorkQueueHealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeFailureHandling;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

@Configuration
@Profile("tokenstore")
public class TokenStoreHealthProbeConfiguration {

    @Autowired
    private TokenStoreHealthProbeProperties tokenStoreHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreAliveScheduledHealthProbe(
            TokenStoreAliveHealthProbe tokenStoreAliveHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                tokenStoreAliveHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                tokenStoreHealthProbeProperties.getAlive().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getAlive().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreReadyScheduledHealthProbe(
            TokenStoreReadyHealthProbe tokenStoreReadyHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                tokenStoreReadyHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                tokenStoreHealthProbeProperties.getReady().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getReady().getCheckInterval());
    }

    @Bean
    @Profile("!single")
    public LdapReplicationHealthProbe tokenStoreReplicationHealthProbe(
            LdapTemplate ldapTemplate,
            ConfigProperties configProperties) {
        return new LdapReplicationHealthProbe("TokenStore replication", ldapTemplate, configProperties);
    }

    @Bean
    public LdapWorkQueueHealthProbe tokenStoreLdapWorkQueueHealthProbe(
            LdapTemplate ldapTemplate) {
        return new LdapWorkQueueHealthProbe("TokenStore work queue", ldapTemplate);
    }

    @Bean
    public LdapConnectionsHealthProbe tokenStoreLdapConnectionsHealthProbe(
            LdapTemplate ldapTemplate) {
        return new LdapConnectionsHealthProbe("TokenStore connections", ldapTemplate);
    }

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
            @Qualifier("tokenStoreReplicationHealthProbe") LdapReplicationHealthProbe ldapReplicationHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapReplicationHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                tokenStoreHealthProbeProperties.getReplication().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getReplication().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreWorkQueueScheduledHealthProbe(
            @Qualifier("tokenStoreLdapWorkQueueHealthProbe") LdapWorkQueueHealthProbe ldapWorkQueueHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapWorkQueueHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                tokenStoreHealthProbeProperties.getWorkQueue().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getWorkQueue().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator tokenStoreConnectionsHealthProbe(
            @Qualifier("tokenStoreLdapConnectionsHealthProbe") LdapConnectionsHealthProbe ldapConnectionsHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapConnectionsHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                tokenStoreHealthProbeProperties.getConnections().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getConnections().getCheckInterval());
    }
}
