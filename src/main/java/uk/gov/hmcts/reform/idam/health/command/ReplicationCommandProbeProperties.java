package uk.gov.hmcts.reform.idam.health.command;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties("replication.healthprobe")
@Getter
@Setter
public class ReplicationCommandProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private String name;
        private String user;
        private String DSUPassword;
        private String DSTPassword;
        private Duration freshnessInterval;
        private Duration checkInterval;
        private String template;
        private String replicationIdentity;
        private Long delayThreshold;
        private Double entryDifferencePercent;
        private Long commandTimeout;
    }

    private ReplicationCommandProbeProperties.Probe command;

}
