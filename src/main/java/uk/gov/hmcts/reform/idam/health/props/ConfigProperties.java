package uk.gov.hmcts.reform.idam.health.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"tokenstore","userstore","replication"})
@ConfigurationProperties
public class ConfigProperties {

    @Getter
    @Setter
    public static class Ldap {

        @Getter
        @Setter
        public static class Replication {
            private Integer delayThreshold;
            private Integer missingUpdatesThreshold;
        }

        private String root;
        private String principal;
        private String userStorePassword;
        private String tokenStorePassword;

        private Replication replication;
    }

    @Getter
    @Setter
    private Ldap ldap;

}
