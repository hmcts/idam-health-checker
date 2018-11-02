package uk.gov.hmcts.reform.idam.health.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"tokenstore","userstore"})
@ConfigurationProperties
public class ConfigProperties {

    @Getter
    @Setter
    public static class Ldap {

        @Getter
        @Setter
        public static class Replication {
            private Integer missingChangesThreshold;
            private Integer pendingUpdatesThreshold;
        }

        private String root;
        private String principal;
        private String password;

        private Replication replication;
    }

    @Getter
    @Setter
    private Ldap ldap;

}
