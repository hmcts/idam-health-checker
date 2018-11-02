package uk.gov.hmcts.reform.idam.health.userstore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("userstore.healthprobe")
@Getter
@Setter
public class UserStoreHealthProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private Long freshnessInterval;
        private Long checkInterval;
    }

    private UserStoreHealthProbeProperties.Probe authentication;
    private UserStoreHealthProbeProperties.Probe replication;
}
