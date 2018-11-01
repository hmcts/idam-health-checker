package uk.gov.hmcts.reform.idam.health.probe.userstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;
import uk.gov.hmcts.reform.idam.health.probe.env.ProbeUserProperties;

@Component
@Profile("userstore")
@Slf4j
public class UserStoreAuthenticationHealthStatus implements HealthStatus {

    private static final String LDAP_PARTITION_SUFFIX = "dc=reform,dc=hmcts,dc=net";
    private static final String LDAP_USER_FILTER_TEMPLATE = "(uid=%s)";

    private final LdapTemplate ldapTemplate;

    private final String ldapUserFilter;
    private final String ldapUserPassword;

    public UserStoreAuthenticationHealthStatus(LdapTemplate ldapTemplate, ProbeUserProperties testUserProperties) {
        this.ldapTemplate = ldapTemplate;

        this.ldapUserFilter = String.format(LDAP_USER_FILTER_TEMPLATE, testUserProperties.getUsername());
        this.ldapUserPassword = testUserProperties.getPassword();
    }

    @Override
    public Status determineStatus() {
        try {
            boolean isAuthenticationSuccessful = ldapTemplate.authenticate(
                    LDAP_PARTITION_SUFFIX,
                    ldapUserFilter,
                    ldapUserPassword);
            return isAuthenticationSuccessful ? Status.UP : Status.DOWN;
        } catch (Exception e) {
            log.error("USERSTORE: " + e.getMessage());
            return Status.DOWN;
        }
    }
}
