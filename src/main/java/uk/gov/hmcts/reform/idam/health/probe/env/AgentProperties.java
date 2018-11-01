package uk.gov.hmcts.reform.idam.health.probe.env;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("web.admin.client")
@Getter
@Setter
public class AgentProperties {

    private String name = "hmcts";
    private String secret;

}
