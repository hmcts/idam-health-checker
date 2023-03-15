package uk.gov.hmcts.reform.idam.health.userstore;

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
@Profile("userstore")
public class UserStoreHealthProbeConfiguration {

    @Autowired
    private UserStoreHealthProbeProperties userStoreHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator userStoreAliveScheduledHealthProbe(
            UserStoreAliveHealthProbe userStoreAliveHealthProbe ) {
        return new ScheduledHealthProbeIndicator(
                userStoreAliveHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                userStoreHealthProbeProperties.getAlive().getFreshnessInterval(),
                userStoreHealthProbeProperties.getAlive().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator userStoreReadyScheduledHealthProbe(
            UserStoreReadyHealthProbe userStoreReadyHealthProbe ) {
        return new ScheduledHealthProbeIndicator(
                userStoreReadyHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                userStoreHealthProbeProperties.getReady().getFreshnessInterval(),
                userStoreHealthProbeProperties.getReady().getCheckInterval());
    }

    @Bean
    @Profile("!single")
    public LdapReplicationHealthProbe userStoreReplicationHealthProbe(
            LdapTemplate ldapTemplate,
            ConfigProperties configProperties) {
        return new LdapReplicationHealthProbe("UserStore replication", ldapTemplate, configProperties);
    }

    @Bean
    public LdapWorkQueueHealthProbe userStoreLdapWorkQueueHealthProbe(
            LdapTemplate ldapTemplate) {
        return new LdapWorkQueueHealthProbe("UserStore work queue", ldapTemplate);
    }

    @Bean
    public LdapConnectionsHealthProbe userStoreLdapConnectionsHealthProbe(
            LdapTemplate ldapTemplate) {
        return new LdapConnectionsHealthProbe("UserStore connections", ldapTemplate);
    }

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
            @Qualifier("userStoreReplicationHealthProbe") LdapReplicationHealthProbe ldapReplicationHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapReplicationHealthProbe,
                HealthProbeFailureHandling.MARK_AS_DOWN,
                taskScheduler,
                userStoreHealthProbeProperties.getReplication().getFreshnessInterval(),
                userStoreHealthProbeProperties.getReplication().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator userStoreWorkQueueScheduledHealthProbe(
            @Qualifier("userStoreLdapWorkQueueHealthProbe") LdapWorkQueueHealthProbe ldapWorkQueueHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapWorkQueueHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                userStoreHealthProbeProperties.getWorkQueue().getFreshnessInterval(),
                userStoreHealthProbeProperties.getWorkQueue().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeIndicator userStoreConnectionsHealthProbe(
            @Qualifier("userStoreLdapConnectionsHealthProbe") LdapConnectionsHealthProbe ldapConnectionsHealthProbe) {
        return new ScheduledHealthProbeIndicator(
                ldapConnectionsHealthProbe,
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                userStoreHealthProbeProperties.getConnections().getFreshnessInterval(),
                userStoreHealthProbeProperties.getConnections().getCheckInterval());
    }
}
