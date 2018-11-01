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
    static class Authentication {

        private Long freshnessInterval;
        private Long checkInterval;
    }

    private Authentication authentication;
}
