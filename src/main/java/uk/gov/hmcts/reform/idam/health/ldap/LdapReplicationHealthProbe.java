package uk.gov.hmcts.reform.idam.health.ldap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Component
@Slf4j
@Profile({"tokenstore","userstore"})
public class LdapReplicationHealthProbe implements HealthProbe {

    private final String TAG = "LDAP Replication: ";

    private static final String BASE_DN = "cn=Replication,cn=monitor";
    private static final String STATUS_ATTRIBUTE = "status";
    private static final String PENDING_UPDATES_ATTRIBUTE = "pending-updates";
    private static final String MISSING_CHANGES_ATTRIBUTE = "missing-changes";
    private static final String APPROXIMATE_DELAY_ATTRIBUTE = "approximate-delay";
    private static final String RECEIVED_UPDATES_ATTRIBUTE = "received-updates";
    private static final String REPLAYED_UPDATES_ATTRIBUTE = "replayed-updates";
    private static final String REPLICATION_FILTER = "(&(objectClass=*)(domain-name=dc=reform,dc=hmcts,dc=net))";
    private static final String NORMAL_STATUS = "normal";

    private static final List<String> EXCLUDED_CNS = new ArrayList<>();
    static {
        EXCLUDED_CNS.add("cn=monitor");
        EXCLUDED_CNS.add("cn=Replication");
        EXCLUDED_CNS.add("cn=dc_reform_dc_hmcts_dc_net");
    }

    private final LdapTemplate ldapTemplate;
    private final ConfigProperties.Ldap ldapProperties;
    private final ReplicationContextMapper replicationContextMapper;

    public LdapReplicationHealthProbe(
            LdapTemplate ldapTemplate,
            ConfigProperties configProperties) {
        this.ldapTemplate = ldapTemplate;
        this.replicationContextMapper = new ReplicationContextMapper();
        this.ldapProperties = configProperties.getLdap();
    }

    @Override
    public boolean probe() {
        try {
            LdapQuery replicationQuery = LdapQueryBuilder.query()
                    .base(BASE_DN)
                    .searchScope(SearchScope.SUBTREE)
                    .attributes(STATUS_ATTRIBUTE, PENDING_UPDATES_ATTRIBUTE, MISSING_CHANGES_ATTRIBUTE, APPROXIMATE_DELAY_ATTRIBUTE, RECEIVED_UPDATES_ATTRIBUTE, REPLAYED_UPDATES_ATTRIBUTE)
                    .filter(REPLICATION_FILTER);

            List<ReplicationInfo> replicationDataList = ldapTemplate.search(replicationQuery, replicationContextMapper);

            boolean result = true;
            for (ReplicationInfo replicationData : replicationDataList) {
                if (replicationData.changelog) {
                    continue;
                }

                if (replicationData.infoRecordType == InfoRecordType.SELF_DIR) {

                    if ((replicationData.status != null) && (NORMAL_STATUS.equals(replicationData.status))) {
                        log.error(TAG + "Failing status checks, " + replicationData.toString());
                        result = false;
                    } else {
                        log.info(TAG + replicationData.infoRecordType + " okay, " + replicationData.toString());
                    }

                } else if (replicationData.infoRecordType != InfoRecordType.UNKNOWN) {

                    if (!isReplicationInGoodState(replicationData)) {
                        log.error(TAG + "Failing good state checks, " + replicationData.toString());
                        result = false;
                    } else {
                        log.info(TAG + replicationData.infoRecordType + " okay, " + replicationData.toString());
                    }

                } else {

                    log.warn(TAG + "Unknown replication record, " + replicationData.toString());

                }

            }
            return result;

        } catch (Exception e) {
            log.error(TAG +  e.getMessage() + " [" + e.getClass().getSimpleName() + "]");
        }
        return false;
    }

    private boolean isReplicationInGoodState(ReplicationInfo info) {
        return info.missingChanges <= this.ldapProperties.getReplication().getMissingChangesThreshold()
                && info.pendingUpdates <= this.ldapProperties.getReplication().getPendingUpdatesThreshold();
    }

    static class ReplicationInfo {
        protected InfoRecordType infoRecordType;
        protected String dn;
        protected boolean changelog;
        protected List<String> cn;
        protected String connectedReplicationServer;
        protected String connectedDirectoryServer;
        protected String directoryServer;
        protected String replicationServer;
        protected String status;
        protected Integer pendingUpdates;
        protected Integer missingChanges;
        protected Integer approximateDelay;
        protected Integer receivedUpdates;
        protected Integer replayedUpdates;

        @Override
        public String toString() {
            List<String> display = new ArrayList<>();
            if (replicationServer != null) {
                display.add("repl-server:" + replicationServer);
            }
            if (directoryServer != null) {
                display.add("dir-server:" + directoryServer);
            }
            if (connectedReplicationServer != null) {
                display.add("repl-connected:" + connectedReplicationServer);
            }
            if (connectedDirectoryServer != null) {
                display.add("dir-connected:" + connectedDirectoryServer);
            }
            if (display.isEmpty()) {
                display.add("dn:" + dn);
            }
            display.add("status:" + status);
            display.add("pending:" + pendingUpdates);
            display.add("missing:" + missingChanges);
            display.add("received:" + receivedUpdates);
            display.add("replayed:" + replayedUpdates);
            display.add("delay:" + approximateDelay);
            return String.join(",", display);
        }
    }

    enum InfoRecordType {
        SELF_REPL, SELF_DIR, SELF_BOTH, OTHER_REPL, OTHER_DIR, OTHER_BOTH, UNKNOWN;
    }

    static class ReplicationContextMapper implements ContextMapper<ReplicationInfo> {

        @Override
        public ReplicationInfo mapFromContext(Object ctx) throws NamingException {
            DirContextAdapter context = (DirContextAdapter)ctx;
            Name dn = context.getDn();
            String statusAttribute = context.getStringAttribute(STATUS_ATTRIBUTE);
            String pendingUpdatesAttribute = context.getStringAttribute(PENDING_UPDATES_ATTRIBUTE);
            String missingChangesAttribute = context.getStringAttribute(MISSING_CHANGES_ATTRIBUTE);
            String approximateDelay = context.getStringAttribute(APPROXIMATE_DELAY_ATTRIBUTE);
            String receivedUpdates = context.getStringAttribute(RECEIVED_UPDATES_ATTRIBUTE);
            String replayedUpdates = context.getStringAttribute(REPLAYED_UPDATES_ATTRIBUTE);

            boolean changelog = false;
            String connectedReplicationServer = null;
            String connectedDirectoryServer = null;
            String replicationServer = null;
            String directoryServer = null;
            List<String> usefulCn = new ArrayList<>();
            for(Enumeration<String> e = dn.getAll(); e.hasMoreElements();) {
                String value = e.nextElement();
                if (!EXCLUDED_CNS.contains(value)) {
                    if (value.startsWith("cn=Changelog")) {
                        changelog = true;
                    } else if (value.startsWith("cn=Connected replication server")) {
                        connectedReplicationServer = value.split("\\) ")[1];
                    } else if (value.startsWith("cn=Connected directory server")) {
                        connectedDirectoryServer = value.split("\\) ")[1];
                    } else if (value.startsWith("cn=Directory server")) {
                        directoryServer = value.split("\\) ")[1];
                    } else if (value.startsWith("cn=Replication server")) {
                        replicationServer = value.split("\\) ")[1];
                    }
                    usefulCn.add(value);
                }
            }

            ReplicationInfo result = new ReplicationInfo();
            result.dn = dn.toString();
            result.cn = usefulCn;
            result.connectedReplicationServer = connectedReplicationServer;
            result.connectedDirectoryServer = connectedDirectoryServer;
            result.directoryServer = directoryServer;
            result.replicationServer = replicationServer;
            result.changelog = changelog;
            result.status = statusAttribute;
            result.pendingUpdates = toInt(pendingUpdatesAttribute);
            result.missingChanges = toInt(missingChangesAttribute);
            result.approximateDelay = toInt(approximateDelay);
            result.receivedUpdates = toInt(receivedUpdates);
            result.replayedUpdates = toInt(replayedUpdates);
            result.infoRecordType = getType(replicationServer, directoryServer, connectedReplicationServer, connectedDirectoryServer);

            return result;
        }

        public Integer toInt(String value) {
            return value == null ? -1 : Integer.valueOf(value);
        }

        public InfoRecordType getType(String replServer, String dirServer, String connectedReplServer, String connectedDirServer) {
            if (StringUtils.isNoneEmpty(connectedReplServer, connectedDirServer)) {
                return InfoRecordType.OTHER_BOTH;
            } else if (StringUtils.isNotEmpty(connectedReplServer)) {
                return InfoRecordType.OTHER_REPL;
            } else if (StringUtils.isNotEmpty(connectedDirServer)) {
                return InfoRecordType.OTHER_DIR;
            } else if (StringUtils.isNoneEmpty(replServer, dirServer)) {
                return InfoRecordType.SELF_BOTH;
            } else if (StringUtils.isNotEmpty(replServer)) {
                return InfoRecordType.SELF_REPL;
            } else if (StringUtils.isNotEmpty(dirServer)) {
                return InfoRecordType.SELF_DIR;
            } else {
                return InfoRecordType.UNKNOWN;
            }
        }
    }
}
