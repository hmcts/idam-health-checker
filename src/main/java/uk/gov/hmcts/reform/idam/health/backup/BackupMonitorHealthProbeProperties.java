package uk.gov.hmcts.reform.idam.health.backup;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("backup.healthprobe")
@Getter
@Setter
public class BackupMonitorHealthProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private String name;
        private Long freshnessInterval;
        private Long checkInterval;
        private String path;
        private Long expiryInterval;
    }

    private BackupMonitorHealthProbeProperties.Probe full;

}
