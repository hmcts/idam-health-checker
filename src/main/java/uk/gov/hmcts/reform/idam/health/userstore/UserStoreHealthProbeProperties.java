package uk.gov.hmcts.reform.idam.health.userstore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties("userstore.healthprobe")
@Getter
@Setter
public class UserStoreHealthProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private Duration freshnessInterval;
        private Duration checkInterval;
    }

    private UserStoreHealthProbeProperties.Probe authentication;
    private UserStoreHealthProbeProperties.Probe replication;
    private UserStoreHealthProbeProperties.Probe workQueue;
    private UserStoreHealthProbeProperties.Probe connections;
    private UserStoreHealthProbeProperties.Probe alive;
    private UserStoreHealthProbeProperties.Probe ready;
}
