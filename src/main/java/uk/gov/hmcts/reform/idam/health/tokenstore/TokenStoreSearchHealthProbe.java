package uk.gov.hmcts.reform.idam.health.tokenstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import javax.naming.directory.SearchControls;
import java.util.List;

@Component
@Profile("tokenstore")
@Slf4j
public class TokenStoreSearchHealthProbe implements HealthProbe {

    private final String TAG = "TokenStore Search: ";

    private static final String LDAP_SEARCH_IN_REPLICATION = "cn=Replication,cn=monitor";
    private static final String LDAP_SEARCH_ANY_OBJECT = "(objectClass=*)";
    private static final String LDAP_CN_ATTRIBUTE = "cn";

    private final LdapTemplate ldapTemplate;

    public TokenStoreSearchHealthProbe(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public boolean probe() {
        try {
            List<Object> searchResponse = ldapTemplate.search(
                    LDAP_SEARCH_IN_REPLICATION,
                    LDAP_SEARCH_ANY_OBJECT,
                    SearchControls.SUBTREE_SCOPE,
                    (AttributesMapper<Object>) attrs -> attrs.get(LDAP_CN_ATTRIBUTE).get());
            if (!searchResponse.isEmpty()) {
                log.info(TAG + "success");
                return true;
            } else {
                log.error(TAG + "response is empty");
            }
        } catch (Exception e) {
            log.error(TAG +  e.getMessage() + " [" + e.getClass().getSimpleName() + "]");
        }
        return false;
    }
}
