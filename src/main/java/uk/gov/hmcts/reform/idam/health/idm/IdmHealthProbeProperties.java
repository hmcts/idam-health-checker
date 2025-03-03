package uk.gov.hmcts.reform.idam.health.idm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties("idm.healthprobe")
@Getter
@Setter
public class IdmHealthProbeProperties {

    @Getter
    @Setter
    static class Probe {
        private Duration freshnessInterval;
        private Duration checkInterval;
    }

    @Getter
    @Setter
    static class RoleExistsProbe {
        private Duration freshnessInterval;
        private Duration checkInterval;
        private String idmClientId;
        private String idmClientSecret;
        private String idmClientScope;
        private String amUser;
        private String amPassword;
        private String amHost;
        private String roleId;
    }

    private IdmHealthProbeProperties.Probe ping;
    private IdmHealthProbeProperties.RoleExistsProbe checkRoleExists;

}
