package uk.gov.hmcts.reform.idam.health.ldap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

@Component
@Slf4j
public class LdapReplicationHealthProbe implements HealthProbe {

    private static final String BASE_DN = "cn=Replication,cn=monitor";
    private static final String STATUS_ATTRIBUTE = "status";
    private static final String PENDING_UPDATES_ATTRIBUTE = "pending-updates";
    private static final String MISSING_CHANGES_ATTRIBUTE = "missing-changes";
    private static final String REPLICATION_FILTER = "(&(objectClass=*)(domain-name=dc=reform,dc=hmcts,dc=net))";
    private static final String NORMAL_STATUS = "normal";

    private final LdapTemplate ldapTemplate;
    private final ReplicationAttributeMapper replicationAttributeMapper;

    public LdapReplicationHealthProbe(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
        this.replicationAttributeMapper = new ReplicationAttributeMapper();
    }

    @Override
    public boolean probe() {
        try {
            LdapQuery replicationQuery = LdapQueryBuilder.query()
                    .base(BASE_DN)
                    .searchScope(SearchScope.SUBTREE)
                    .attributes(STATUS_ATTRIBUTE, PENDING_UPDATES_ATTRIBUTE, MISSING_CHANGES_ATTRIBUTE)
                    .filter(REPLICATION_FILTER);

            List<ReplicationInfo> replicationData = ldapTemplate.search(replicationQuery, replicationAttributeMapper);
            if (replicationData.stream().noneMatch(info -> NORMAL_STATUS.equalsIgnoreCase(info.status))) {
                return false;
            }
            return replicationData.stream().allMatch(info -> info.missingChanges == 0 && info.pendingUpdates == 0);
        } catch (Exception e) {
            log.error("Ldap-Repl " + e.getMessage());
        }
        return false;
    }

    static class ReplicationInfo {
        private String status;
        private Integer pendingUpdates;
        private Integer missingChanges;
    }

    static class ReplicationAttributeMapper implements AttributesMapper<ReplicationInfo> {

        @Override
        public ReplicationInfo mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute statusAttribute = attributes.get(STATUS_ATTRIBUTE);
            Attribute pendingUpdatesAttribute = attributes.get(PENDING_UPDATES_ATTRIBUTE);
            Attribute missingChangesAttribute = attributes.get(MISSING_CHANGES_ATTRIBUTE);

            ReplicationInfo result = new ReplicationInfo();
            result.status = statusAttribute == null ? null : statusAttribute.get().toString();
            result.pendingUpdates = pendingUpdatesAttribute == null ? 0 : Integer.valueOf(pendingUpdatesAttribute.get().toString());
            result.missingChanges = missingChangesAttribute == null ? 0 : Integer.valueOf(missingChangesAttribute.get().toString());

            return result;
        }
    }
}
