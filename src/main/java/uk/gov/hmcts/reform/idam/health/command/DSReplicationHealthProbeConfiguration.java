package uk.gov.hmcts.reform.idam.health.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeFailureHandling;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

@Configuration
@Profile("replication")
public class DSReplicationHealthProbeConfiguration {

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator replicationMonitor(ConfigProperties configProperties, ReplicationCommandProbeProperties commandProbeProperties) {
        return new ScheduledHealthProbeIndicator(
                new ReplicationCommandProbe(
                        commandProbeProperties.getCommand().getName(),
                        commandProbeProperties.getCommand().getTemplate(),
                        configProperties.getLdap().getPassword(),
                        commandProbeProperties.getCommand().getHostname()
                ),
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                commandProbeProperties.getCommand().getFreshnessInterval(),
                commandProbeProperties.getCommand().getCheckInterval()
        );
    }

}
