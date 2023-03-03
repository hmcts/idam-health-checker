package uk.gov.hmcts.reform.idam.health.am;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("am.healthprobe")
@Getter
@Setter
public class AmHealthProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private Long freshnessInterval;
        private Long checkInterval;
    }

    private AmHealthProbeProperties.Probe live;
    private AmHealthProbeProperties.Probe passwordGrant;

    @Getter
    @Setter
    static class Identity {
        private String host;
        private String scope;
    }

    private AmHealthProbeProperties.Identity identity;

}
