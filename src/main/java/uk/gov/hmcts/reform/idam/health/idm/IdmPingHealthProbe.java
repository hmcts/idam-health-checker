package uk.gov.hmcts.reform.idam.health.idm;

import lombok.CustomLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.util.Base64;
import java.util.Map;

@Component
@Profile("idm")
@CustomLog
public class IdmPingHealthProbe extends HealthProbe {

    private static final String TAG = "IDM Ping: ";

    private static final String STATE = "state";
    private static final String IDM_ACTIVE = "ACTIVE_READY";

    private static final String LDAP_CHECK_ENABLED_FIELD = "enabled";

    private static String ANONYMOUS_USER = "anonymous";
    private static String ANONYMOUS_PASSWORD = "anonymous";

    private final IdmProvider idmProvider;
    private final String authorization;
    private final String ldapCheckAuthorization;
    private final IdmHealthProbeProperties idmHealthProbeProperties;

    public IdmPingHealthProbe(IdmProvider idmProvider, IdmHealthProbeProperties idmHealthProbeProperties) {
        this.idmProvider = idmProvider;
        this.authorization = "Basic " + encode(ANONYMOUS_USER, ANONYMOUS_PASSWORD);

        final String ldapCheckUsername = StringUtils.defaultString(idmHealthProbeProperties.getLdapCheck().getUsername(), "");
        final String ldapCheckPassword = StringUtils.defaultString(idmHealthProbeProperties.getLdapCheck().getPassword(), "");
        this.ldapCheckAuthorization = "Basic " + encode(ldapCheckUsername, ldapCheckPassword);

        this.idmHealthProbeProperties = idmHealthProbeProperties;
    }

    /**
     * @should pass when ping and ldap are ok
     * @should fail when ping state is unexpected
     * @should fail when ping state is missing
     * @should fail when ping response is empty
     * @should fail when ping response is null
     * @should fail when ping throws exception
     * @should fail when ldap enabled is false
     * @should fail when ldap enabled is unexpected
     * @should fail when ldap enabled is missing
     * @should fail when ldap response is empty
     * @should fail when ldap response is null
     * @should fail when ldap throws exception
     * @should pass when ping is ok and ldap check if configured to skip
     * @should fail if ldap check is missing credentials
     */
    @Override
    public boolean probe() {
        try {
            final Map<String, String> pingResponse = idmProvider.ping(authorization);
            final boolean pingOk = IDM_ACTIVE.equals(MapUtils.getString(pingResponse, STATE));

            if (!pingOk) {
                log.error(TAG + "idm ping response did not contain expected value");
                return false;
            }

            boolean checkLdap = idmHealthProbeProperties.getLdapCheck().getEnabled();

            if (!checkLdap) {
                log.info(TAG + "idm ping success, skipping ldap check, disabled per configuration");
                return true;
            }

            log.info(TAG + "idm ping success, checking if ldap connection is enabled");

            final String ldapCheckUsername = idmHealthProbeProperties.getLdapCheck().getUsername();
            final String ldapCheckPassword = idmHealthProbeProperties.getLdapCheck().getPassword();
            if (ldapCheckUsername == null && ldapCheckPassword == null) {
                throw new RuntimeException("IDM ldap check is enabled but has no credentials set");
            }

            final Map<String, Object> ldapResponse = idmProvider.checkLdap(ldapCheckAuthorization, LDAP_CHECK_ENABLED_FIELD);
            final Boolean ldapOk = MapUtils.getBoolean(ldapResponse, LDAP_CHECK_ENABLED_FIELD, false);

            if (!ldapOk) {
                log.error(TAG + "idm ldap response did not contain expected value");
                return false;
            }

            log.info(TAG + "idm success, ping and ldap ok");

        } catch (Exception e) {
            log.error(TAG +  e.getMessage() + " [" + e.getClass().getSimpleName() + "]");
            return false;
        }

        return true;
    }

    private String encode(String identity, String secret) {
        return Base64.getEncoder().encodeToString((identity + ":" + secret).getBytes());
    }
}
