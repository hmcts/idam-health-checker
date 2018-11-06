package uk.gov.hmcts.reform.idam.health.tokenstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeExecutor;

@Configuration
@Profile("tokenstore")
public class TokenStoreHealthProbeConfiguration {

    @Autowired
    private TokenStoreHealthProbeProperties tokenStoreHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeExecutor tokenStoreSearchScheduledHealthProbe(
            TokenStoreSearchHealthProbe tokenStoreSearchHealthProbe) {
        return new ScheduledHealthProbeExecutor(
                tokenStoreSearchHealthProbe,
                taskScheduler,
                tokenStoreHealthProbeProperties.getSearch().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getSearch().getCheckInterval());
    }

    @Bean
    public ScheduledHealthProbeExecutor tokenStoreReplicationScheduledHealthProbe(
            LdapReplicationHealthProbe ldapReplicationHealthProbe) {
        return new ScheduledHealthProbeExecutor(
                ldapReplicationHealthProbe,
                taskScheduler,
                tokenStoreHealthProbeProperties.getReplication().getFreshnessInterval(),
                tokenStoreHealthProbeProperties.getReplication().getCheckInterval());
    }
}
