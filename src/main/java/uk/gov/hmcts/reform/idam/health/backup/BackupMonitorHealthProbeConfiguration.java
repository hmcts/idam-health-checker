package uk.gov.hmcts.reform.idam.health.backup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbeFailureHandling;
import uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator;

@Configuration
@Profile("backup")
public class BackupMonitorHealthProbeConfiguration {

    @Autowired
    private BackupMonitorHealthProbeProperties backupMonitorHealthProbeProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Bean
    public ScheduledHealthProbeIndicator fullBackupMonitor() {
        return new ScheduledHealthProbeIndicator(
                new FileFreshnessProbe(
                        backupMonitorHealthProbeProperties.getFull().getName(),
                        backupMonitorHealthProbeProperties.getFull().getPath(),
                        backupMonitorHealthProbeProperties.getFull().getExpiryInterval()
                ),
                HealthProbeFailureHandling.IGNORE,
                taskScheduler,
                backupMonitorHealthProbeProperties.getFull().getFreshnessInterval(),
                backupMonitorHealthProbeProperties.getFull().getCheckInterval()
        );
    }

}
