package uk.gov.hmcts.reform.idam.health.ldap;

import lombok.CustomLog;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
public class LdapConnectionsHealthProbe extends LdapQueryHealthProbe<LdapConnectionsHealthProbe.ConnectionsInfo> {

    private static final String BASE_DN = "cn=LDAPS,cn=connection handlers,cn=monitor";
    private static final String CONNECTION_FILTER = "(objectClass=ds-monitor-connection-handler)";

    private static final String ACTIVE_CONNECTIONS_COUNT_ATTRIBUTE = "ds-mon-active-connections-count";
    private static final String CONNECTIONS_STATS_ATTRIBUTE = "ds-mon-connections";
    private static final String ACTIVE_PERSISTENT_SEARCH_COUNT_ATTRIBUTE = "ds-mon-active-persistent-searches";
    private static final String ABANDONED_REQUESTS_COUNT_ATTRIBUTE = "ds-mon-abandoned-requests";
    private static final String ABANDONED_REQUESTS_STATS_ATTRIBUTE = "ds-mon-requests-abandon";
    private static final String FAILURE_CLIENT_INVALID_STATS_ATTRIBUTE = "ds-mon-requests-failure-client-invalid-request";
    private static final String FAILURE_CLIENT_SECURITY_STATS_ATTRIBUTE = "ds-mon-requests-failure-client-security";
    private static final String FAILURE_CLIENT_RESOURCE_STATS_ATTRIBUTE = "ds-mon-requests-failure-client-resource-limit";
    private static final String FAILURE_CLIENT_REFERRAL_STATS_ATTRIBUTE = "ds-mon-requests-failure-client-referral";
    private static final String FAILURE_SERVER_STATS_ATTRIBUTE = "ds-mon-requests-failure-server";
    private static final String FAILURE_UNCATEGORIZED_STATS_ATTRIBUTE = "ds-mon-requests-failure-uncategorized";

    public LdapConnectionsHealthProbe(String probeName, LdapTemplate ldapTemplate) {
        super(probeName, ldapTemplate, new ConnectionsContextMapper());
    }

    @Override
    public LdapQuery ldapQuery() {
        return LdapQueryBuilder.query()
                .base(BASE_DN)
                .searchScope(SearchScope.SUBTREE)
                .attributes(ACTIVE_CONNECTIONS_COUNT_ATTRIBUTE,
                        CONNECTIONS_STATS_ATTRIBUTE,
                        ACTIVE_PERSISTENT_SEARCH_COUNT_ATTRIBUTE,
                        ABANDONED_REQUESTS_COUNT_ATTRIBUTE,
                        ABANDONED_REQUESTS_STATS_ATTRIBUTE,
                        FAILURE_CLIENT_INVALID_STATS_ATTRIBUTE,
                        FAILURE_CLIENT_SECURITY_STATS_ATTRIBUTE,
                        FAILURE_CLIENT_RESOURCE_STATS_ATTRIBUTE,
                        FAILURE_CLIENT_REFERRAL_STATS_ATTRIBUTE,
                        FAILURE_SERVER_STATS_ATTRIBUTE,
                        FAILURE_UNCATEGORIZED_STATS_ATTRIBUTE)
                .filter(CONNECTION_FILTER);
    }

    @Override
    public boolean handleResult(List<ConnectionsInfo> resultList) {
        log.info("{}: {}", getName(), resultList.stream().map(LdapConnectionsHealthProbe.ConnectionsInfo::toString).collect(Collectors.joining("; ")));
        return true;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @EqualsAndHashCode
    static class ConnectionsInfo {
        String dn;
        String activeConnectionCount;
        String connectionStats;
        String activePersistentSearchCount;
        String abandonedRequestCount;
        String abandonedRequestStats;
        String failureClientInvalidStats;
        String failureClientSecurityStats;
        String failureClientResourceStats;
        String failureClientReferralStats;
        String failureServerStats;
        String failureUncategorizedStats;

        @Override
        public String toString() {
            return "ConnectionsInfo: {" +
                    "\"dn\":\"" + dn + "\"" +
                    ", \"activeConnectionCount\": \"" + activeConnectionCount + "\"" +
                    ", \"activePersistentSearchCount\": \"" + activePersistentSearchCount + "\"" +
                    ", \"connectionStats\": " + connectionStats +
                    ", \"abandonedRequestCount\": " + abandonedRequestCount + 
                    ", \"abandonedRequestStats\": " + abandonedRequestStats + 
                    ", \"failureClientInvalidStats\": " + failureClientInvalidStats + 
                    ", \"failureClientSecurityStats\": " + failureClientSecurityStats + 
                    ", \"failureClientResourceStats\": " + failureClientResourceStats + 
                    ", \"failureClientReferralStats\": " + failureClientReferralStats + 
                    ", \"failureServerStats\": " + failureServerStats + 
                    ", \"failureUncategorizedStats\": " + failureUncategorizedStats + 
                    '}';
        }
    }

    static class ConnectionsContextMapper implements ContextMapper<LdapConnectionsHealthProbe.ConnectionsInfo> {

        @Override
        public LdapConnectionsHealthProbe.ConnectionsInfo mapFromContext(Object ctx) throws NamingException {
            DirContextAdapter context = (DirContextAdapter) ctx;
            Name dn = context.getDn();

            log.debug(context.toString());

            ConnectionsInfo connectionsInfo = new ConnectionsInfo();
            connectionsInfo.dn = dn.toString();
            connectionsInfo.activeConnectionCount = context.getStringAttribute(ACTIVE_CONNECTIONS_COUNT_ATTRIBUTE);
            connectionsInfo.connectionStats = context.getStringAttribute(CONNECTIONS_STATS_ATTRIBUTE);
            connectionsInfo.activePersistentSearchCount = context.getStringAttribute(ACTIVE_PERSISTENT_SEARCH_COUNT_ATTRIBUTE);
            connectionsInfo.abandonedRequestCount = context.getStringAttribute(ABANDONED_REQUESTS_COUNT_ATTRIBUTE);
            connectionsInfo.abandonedRequestStats = context.getStringAttribute(ABANDONED_REQUESTS_STATS_ATTRIBUTE);
            connectionsInfo.failureClientInvalidStats = context.getStringAttribute(FAILURE_CLIENT_INVALID_STATS_ATTRIBUTE);
            connectionsInfo.failureClientSecurityStats = context.getStringAttribute(FAILURE_CLIENT_SECURITY_STATS_ATTRIBUTE);
            connectionsInfo.failureClientResourceStats = context.getStringAttribute(FAILURE_CLIENT_RESOURCE_STATS_ATTRIBUTE);
            connectionsInfo.failureClientReferralStats = context.getStringAttribute(FAILURE_CLIENT_REFERRAL_STATS_ATTRIBUTE);
            connectionsInfo.failureServerStats = context.getStringAttribute(FAILURE_SERVER_STATS_ATTRIBUTE);
            connectionsInfo.failureUncategorizedStats = context.getStringAttribute(FAILURE_UNCATEGORIZED_STATS_ATTRIBUTE);

            return connectionsInfo;
        }
    }

}
