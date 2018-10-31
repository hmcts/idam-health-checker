package uk.gov.hmcts.reform.idam.health.probe.userstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

@Component
@Profile("userstore")
@Slf4j
public class UserStoreAuthenticationHealthStatus implements HealthStatus {

    private final LdapTemplate ldapTemplate;

    public UserStoreAuthenticationHealthStatus(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public Status determineStatus() {
        try {
            boolean authenticateResponse = ldapTemplate
                    .authenticate("", "uid=dummy", "");
            return authenticateResponse ? Status.UP : Status.DOWN;
        } catch (Exception e) {
            log.error("USERSTORE: " + e.getMessage());
            return Status.DOWN;
        }
    }
}
