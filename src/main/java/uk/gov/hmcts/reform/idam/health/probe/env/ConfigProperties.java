package uk.gov.hmcts.reform.idam.health.probe.env;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class ConfigProperties {

    @Getter
    @Setter
    public static class Ldap {
        private String root;
        private String principal;
        private String password;
    }

    @Getter
    @Setter
    private Ldap ldap;

    private String bindpasswd;

    public void setBindpasswd(String bindPassword) {
        this.bindpasswd = bindPassword;
        this.ldap.password = bindPassword;
    }
}
