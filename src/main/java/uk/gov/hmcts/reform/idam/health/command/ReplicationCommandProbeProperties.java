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
        private Long freshnessInterval;
        private Long checkInterval;
        private String template;
        private String hostname;
    }

    private ReplicationCommandProbeProperties.Probe command;

}
