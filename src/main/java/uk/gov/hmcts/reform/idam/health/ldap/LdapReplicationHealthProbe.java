package uk.gov.hmcts.reform.idam.health.ldap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@Profile({"tokenstore","userstore"})
public class LdapReplicationHealthProbe implements HealthProbe {

    private final String TAG = "LDAP Replication: ";

    private static final String BASE_DN = "cn=Replication,cn=monitor";
    private static final String STATUS_ATTRIBUTE = "status";
    private static final String PENDING_UPDATES_ATTRIBUTE = "pending-updates";
    private static final String MISSING_CHANGES_ATTRIBUTE = "missing-changes";
    private static final String REPLICATION_FILTER = "(&(objectClass=*)(domain-name=dc=reform,dc=hmcts,dc=net))";
    private static final String NORMAL_STATUS = "normal";

    private final LdapTemplate ldapTemplate;
    private final ConfigProperties.Ldap ldapProperties;
    private final ReplicationAttributeMapper replicationAttributeMapper;

    public LdapReplicationHealthProbe(
            LdapTemplate ldapTemplate,
            ConfigProperties configProperties) {
        this.ldapTemplate = ldapTemplate;
        this.replicationAttributeMapper = new ReplicationAttributeMapper();
        this.ldapProperties = configProperties.getLdap();
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
                log.error(TAG + "Failing status checks, " + toString(summarize(replicationData)));
                return false;
            }
            if (replicationData.stream().allMatch(this::isReplicationInGoodState)) {
                log.info(TAG + "success, checked " + replicationData.size() + " results");
                return true;
            } else {
                log.error(TAG + "Failing missing or pending changes, " + toString(summarize(replicationData)));
                return false;
            }
        } catch (Exception e) {
            log.error(TAG +  e.getMessage() + " [" + e.getClass().getSimpleName() + "]");
        }
        return false;
    }

    private boolean isReplicationInGoodState(ReplicationInfo info) {
        return info.missingChanges <= this.ldapProperties.getReplication().getMissingChangesThreshold()
                && info.pendingUpdates <= this.ldapProperties.getReplication().getPendingUpdatesThreshold();
    }

    private Map<String, Integer> summarize(List<ReplicationInfo> replicationData) {
        Map<String, Integer> summary = new HashMap<>();
        for (ReplicationInfo replicationDatum : replicationData) {
            increment(summary, "status-" + replicationDatum.status, 1);
            increment(summary, replicationDatum.status + "-missing", replicationDatum.missingChanges);
            increment(summary, replicationDatum.status + "-pending", replicationDatum.pendingUpdates);
        }
        return summary;
    }

    private String toString(Map<String, Integer> summary) {
        if (!summary.isEmpty()) {
            return summary.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(","));
        }
        return "no replication info";
    }

    private void increment(Map<String, Integer> summary, String key, int value) {
        Integer statusCount = summary.get(key);
        if (statusCount == null) {
            statusCount = value;
        } else {
            statusCount += value;
        }
        summary.put(key, statusCount);
    }

    static class ReplicationInfo {
        protected String status;
        protected Integer pendingUpdates;
        protected Integer missingChanges;
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
