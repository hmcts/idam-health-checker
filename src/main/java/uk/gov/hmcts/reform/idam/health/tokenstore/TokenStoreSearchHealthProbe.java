package uk.gov.hmcts.reform.idam.health.tokenstore;

import lombok.CustomLog;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import javax.naming.directory.SearchControls;
import java.util.List;

@Component
@Profile("tokenstore")
@CustomLog
public class TokenStoreSearchHealthProbe extends HealthProbe {

    private final String TAG = "TokenStore Search: ";

    private static final String LDAP_SEARCH_IN_CONFIG = "cn=schema providers,cn=config";
    private static final String LDAP_SEARCH_ANY_OBJECT = "(objectClass=*)";
    private static final String LDAP_CN_ATTRIBUTE = "cn";

    private final LdapTemplate ldapTemplate;

    public TokenStoreSearchHealthProbe(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean probe() {
        try {
            List<Object> searchResponse = ldapTemplate.search(
                    LDAP_SEARCH_IN_CONFIG,
                    LDAP_SEARCH_ANY_OBJECT,
                    SearchControls.SUBTREE_SCOPE,
                    (AttributesMapper<Object>) attrs -> attrs.get(LDAP_CN_ATTRIBUTE).get());
            if (!searchResponse.isEmpty()) {
                return handleSuccess();
            } else {
                return handleError("response is empty");
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }
}
