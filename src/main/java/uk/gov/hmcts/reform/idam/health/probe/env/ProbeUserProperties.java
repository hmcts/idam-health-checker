package uk.gov.hmcts.reform.idam.health.probe.env;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("system.owner")
@Getter
@Setter
public class ProbeUserProperties {

    private String username;
    private String password;
}
