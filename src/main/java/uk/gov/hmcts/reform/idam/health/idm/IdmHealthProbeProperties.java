package uk.gov.hmcts.reform.idam.health.idm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("idm.healthprobe")
@Getter
@Setter
public class IdmHealthProbeProperties {

    @Getter
    @Setter
    static class Ping {
        private Long freshnessInterval;
        private Long checkInterval;
    }

    private Ping ping;
}
