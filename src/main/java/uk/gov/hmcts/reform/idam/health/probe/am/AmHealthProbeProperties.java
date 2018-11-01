package uk.gov.hmcts.reform.idam.health.probe.am;

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
    static class Ping {
        private Long freshnessInterval;
        private Long checkInterval;
    }

    private AmHealthProbeProperties.Ping ping;

    @Getter
    @Setter
    static class Identity {
        private String username;
        private String password;
        private String client;
        private String clientSecret;
        private String scope;
        private String host;
    }

    private AmHealthProbeProperties.Identity identity;

}
