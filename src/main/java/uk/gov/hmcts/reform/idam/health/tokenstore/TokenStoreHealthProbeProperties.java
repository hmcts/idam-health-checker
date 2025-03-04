package uk.gov.hmcts.reform.idam.health.tokenstore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties("tokenstore.healthprobe")
@Getter
@Setter
public class TokenStoreHealthProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private Duration freshnessInterval;
        private Duration checkInterval;
    }

    private TokenStoreHealthProbeProperties.Probe search;
    private TokenStoreHealthProbeProperties.Probe replication;
    private TokenStoreHealthProbeProperties.Probe workQueue;
    private TokenStoreHealthProbeProperties.Probe connections;
    private TokenStoreHealthProbeProperties.Probe alive;
    private TokenStoreHealthProbeProperties.Probe ready;

}
