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

    private final LdapTemplate ldapTemplate;

    public TokenStoreSearchHealthStatus(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public Status determineStatus() {
        try {
            List<Object> searchResponse = ldapTemplate.search(
                    "cn=Replication,cn=monitor",
                    "(objectClass=*)",
                    SearchControls.SUBTREE_SCOPE,
                           (AttributesMapper) attrs -> attrs.get("cn").get());
            return searchResponse.size() > 0 ? Status.UP : Status.DOWN;
        } catch (Exception e) {
            log.error("TOKENSTORE: " + e.getMessage());
            return Status.DOWN;
        }
    }
}
