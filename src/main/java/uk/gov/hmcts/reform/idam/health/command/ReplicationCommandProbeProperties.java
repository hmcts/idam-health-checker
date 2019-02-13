package uk.gov.hmcts.reform.idam.health.command;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("replication.healthprobe")
@Getter
@Setter
public class ReplicationCommandProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private String name;
        private String password;
        private Long freshnessInterval;
        private Long checkInterval;
        private String template;
        private String hostIdentity;
        private Long missingUpdatesThreshold;
        private Long entryDifferenceThreshold;
        private Long commandTimeout;
    }

    private ReplicationCommandProbeProperties.Probe command;

}
