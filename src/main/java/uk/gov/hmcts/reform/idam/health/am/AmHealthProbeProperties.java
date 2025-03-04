package uk.gov.hmcts.reform.idam.health.am;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties("am.healthprobe")
@Getter
@Setter
public class AmHealthProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private Duration freshnessInterval;
        private Duration checkInterval;
    }

    private AmHealthProbeProperties.Probe live;

    private AmHealthProbeProperties.Probe ready;
    private AmHealthProbeProperties.Probe passwordGrant;

    @Getter
    @Setter
    static class ClientProbe {
        private Duration freshnessInterval;
        private Duration checkInterval;
        private String clientId;
        private String clientScope;
        private String amUser;
        private String amPassword;
    }

    private AmHealthProbeProperties.ClientProbe rootPasswordGrant;

    @Getter
    @Setter
    static class Identity {
        private String host;
        private String scope;
    }

    private AmHealthProbeProperties.Identity identity;

}
