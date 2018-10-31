package uk.gov.hmcts.reform.idam.health.probe.tokenstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthStatus;
import uk.gov.hmcts.reform.idam.health.probe.Status;

import javax.naming.directory.SearchControls;
import java.util.List;

@Component
@Profile("tokenstore")
@Slf4j
public class TokenStoreSearchHealthStatus implements HealthStatus {

    private static final String LDAP_SEARCH_IN_REPLICATION = "cn=Replication,cn=monitor";
    private static final String LDAP_SEARCH_ANY_OBJECT = "(objectClass=*)";
    private static final String LDAP_CN_ATTRIBUTE = "cn";

    private final LdapTemplate ldapTemplate;

    public TokenStoreSearchHealthStatus(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public Status determineStatus() {
        try {
            List<Object> searchResponse = ldapTemplate.search(
                    LDAP_SEARCH_IN_REPLICATION,
                    LDAP_SEARCH_ANY_OBJECT,
                    SearchControls.SUBTREE_SCOPE,
                    (AttributesMapper<Object>) attrs -> attrs.get(LDAP_CN_ATTRIBUTE).get());
            return searchResponse.isEmpty() ? Status.DOWN : Status.UP;
        } catch (Exception e) {
            log.error("TOKENSTORE: " + e.getMessage());
            return Status.DOWN;
        }
    }
}
