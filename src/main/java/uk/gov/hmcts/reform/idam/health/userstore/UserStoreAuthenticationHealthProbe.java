package uk.gov.hmcts.reform.idam.health.userstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.props.ProbeUserProperties;

@Component
@Profile("userstore")
@Slf4j
public class UserStoreAuthenticationHealthProbe implements HealthProbe {

    private static final String LDAP_PARTITION_SUFFIX = "dc=reform,dc=hmcts,dc=net";
    private static final String LDAP_USER_FILTER_TEMPLATE = "(uid=%s)";

    private final LdapTemplate ldapTemplate;

    private final String ldapUserFilter;
    private final String ldapUserPassword;

    public UserStoreAuthenticationHealthProbe(
            LdapTemplate ldapTemplate,
            ProbeUserProperties testUserProperties) {
        this.ldapTemplate = ldapTemplate;

        this.ldapUserFilter = String.format(LDAP_USER_FILTER_TEMPLATE, testUserProperties.getUsername());
        this.ldapUserPassword = testUserProperties.getPassword();
    }

    @Override
    public boolean probe() {
        try {
            boolean isAuthenticationSuccessful = ldapTemplate.authenticate(
                    LDAP_PARTITION_SUFFIX,
                    ldapUserFilter,
                    ldapUserPassword);
            if (isAuthenticationSuccessful) {
                return true;
            } else {
                log.error("UserStore Auth: authentication failed");
            }
        } catch (Exception e) {
            log.error("UserStore Auth: " + e.getMessage());
        }
        return false;
    }
}
