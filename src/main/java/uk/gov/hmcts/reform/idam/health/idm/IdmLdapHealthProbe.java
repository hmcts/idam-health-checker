package uk.gov.hmcts.reform.idam.health.idm;

import lombok.CustomLog;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.util.Base64;
import java.util.Map;

@Component
@Profile("idm")
@CustomLog
public class IdmLdapHealthProbe extends HealthProbe {

    private static final String LDAP_CHECK_ENABLED_FIELD = "enabled";

    private final IdmProvider idmProvider;
    private final String ldapCheckAuthorization;

    public IdmLdapHealthProbe(IdmProvider idmProvider, IdmHealthProbeProperties idmHealthProbeProperties) {
        this.idmProvider = idmProvider;

        final String ldapCheckUsername = idmHealthProbeProperties.getLdapCheck().getUsername();
        final String ldapCheckPassword = idmHealthProbeProperties.getLdapCheck().getPassword();
        if (StringUtils.isNoneEmpty(ldapCheckUsername, ldapCheckPassword)) {
            this.ldapCheckAuthorization = "Basic " + Base64.getEncoder().encodeToString((ldapCheckUsername + ":" + ldapCheckPassword).getBytes());
        } else {
            this.ldapCheckAuthorization = null;
        }
    }

    /**
     * @should pass when ldap is ok
     * @should fail when ldap enabled is unexpected
     * @should fail when ldap enabled is missing
     * @should fail when ldap response is empty
     * @should fail when ldap response is null
     * @should fail when ldap throws exception
     * @should fail if ldap check is missing credentials
     */
    @Override
    public boolean probe() {

        try {

            if (ldapCheckAuthorization == null) {
                return handleError("IDM ldap check has no credentials set");
            }

            final Map<String, Object> ldapResponse = idmProvider.checkLdap(ldapCheckAuthorization, LDAP_CHECK_ENABLED_FIELD);
            final Boolean ldapOk = MapUtils.getBoolean(ldapResponse, LDAP_CHECK_ENABLED_FIELD, false);
            if (ldapOk) {
                return handleSuccess();
            } else {
                handleError("IDM ldap response did not contain expected value");
            }

        } catch (Exception e) {
            handleException(e);
        }

        return false;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
