package uk.gov.hmcts.reform.idam.health.ldap;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.CustomLog;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

import javax.annotation.Nullable;
import javax.naming.Name;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static java.text.MessageFormat.format;

@CustomLog
public class LdapReplicationHealthProbe extends LdapQueryHealthProbe<LdapReplicationHealthProbe.ReplicationInfo> {

    private static final String BASE_DN = "cn=Replication,cn=monitor";
    private static final String REPLICATION_FILTER = "(&(objectClass=*)(ds-mon-domain-name=dc=reform,dc=hmcts,dc=net)(!(cn=Changelog*)))";

    private static final String STATUS_ATTRIBUTE = "ds-mon-status";
    private static final String PENDING_UPDATES_ATTRIBUTE = "ds-mon-updates-outbound-queue";
    private static final String MISSING_CHANGES_ATTRIBUTE = "ds-mon-missing-changes";
    private static final String CURRENT_DELAY = "ds-mon-current-delay";
    private static final String SENT_UPDATES_ATTRIBUTE = "ds-mon-sent-updates";
    private static final String RECEIVED_UPDATES_ATTRIBUTE = "ds-mon-assured-sr-received-updates-acked";
    private static final String REPLAYED_UPDATES_ATTRIBUTE = "ds-mon-replayed-updates";
    private static final String NORMAL_STATUS = "Normal";

    private final ConfigProperties.Ldap ldapProperties;

    private List<ReplicationInfo> replicationInfoState = Collections.emptyList();
    private boolean probeState;

    public LdapReplicationHealthProbe(String probeName,
            LdapTemplate ldapTemplate,
            ConfigProperties configProperties) {
        super(probeName, ldapTemplate, new ReplicationContextMapper());
        this.ldapProperties = configProperties.getLdap();
    }

    @Override
    public LdapQuery ldapQuery() {
        return LdapQueryBuilder.query()
                .base(BASE_DN)
                .searchScope(SearchScope.SUBTREE)
                .attributes(STATUS_ATTRIBUTE,
                        PENDING_UPDATES_ATTRIBUTE,
                        MISSING_CHANGES_ATTRIBUTE,
                        CURRENT_DELAY,
                        SENT_UPDATES_ATTRIBUTE,
                        RECEIVED_UPDATES_ATTRIBUTE,
                        REPLAYED_UPDATES_ATTRIBUTE)
                .filter(REPLICATION_FILTER);
    }

    private List<String> addErrors(List<String> errors, String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
        return errors;
    }

    @Override
    public boolean handleResult(List<ReplicationInfo> resultList) {
        if (CollectionUtils.isEqualCollection(replicationInfoState, resultList)) {
            return probeState;
        }

        List<String> errors = null;
        for (ReplicationInfo record : resultList) {
            switch (record.recordType) {
                case LOCAL_DS:
                    if (!updatesAreBeingReplayed(record)) {
                        errors = addErrors(errors, format("{}: Failing replay check, {}", record.recordType, record.toString()));
                    } else if ((record.status != null) && (!NORMAL_STATUS.equalsIgnoreCase(record.status))) {
                        log.warn("{}: {}: Unexpected status, {}", getName(), record.recordType, record.toString());
                    } else if (log.isInfoEnabled()) {
                        log.info("{}: {}:  okay, {}", getName(), record.recordType, record.toString());
                    }
                    break;
                case LOCAL_RS:
                    if (changesAreMissing(record)) {
                        errors = addErrors(errors, format("{}: Failing missing changes check, {}", record.recordType, record.toString()));
                    } else if (log.isInfoEnabled()) {
                        log.info("{}: {}: okay, {}", getName(), record.recordType, record.toString());
                    }
                    break;
                case LOCAL_RS_CONN_DS:
                    if (!updatesAreOnTime(record)) {
                        errors = addErrors(errors, format("{}: Failing delay check, {}", record.recordType, record.toString()));
                    } else if (log.isInfoEnabled()) {
                        log.info("{}: {}: okay, {}", getName(), record.recordType, record.toString());
                    }
                    break;
                case UNKNOWN:
                    if (log.isDebugEnabled()) {
                        log.debug( "{}: Unknown replication record type, {}", getName(), record.toString());
                    }
                    break;
                default:
                    if (log.isInfoEnabled()) {
                        log.info("{}: {}: okay, {}", getName(), record.recordType, record.toString());
                    }
            }
        }

        replicationInfoState = resultList;

        if (errors == null) {
            probeState = true;
            return handleSuccess();
        } else if (errors.size() == 1) {
            return handleError(errors.get(0));
        } else {
            return handleError("Multiple errors, see log");
        }

    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean handleEmptyResult() {
        return handleResult(Collections.emptyList());
    }

    private boolean updatesAreBeingReplayed(ReplicationInfo info) {
        return (info.receivedUpdates < 0) ||
                (info.replayedUpdates < 0) ||
                (Math.abs(info.receivedUpdates - info.replayedUpdates) <= this.ldapProperties.getReplication().getMissingUpdatesThreshold());
    }

    private boolean updatesAreOnTime(ReplicationInfo info) {
        return (info.currentDelay < 0) ||
                (info.currentDelay <= this.ldapProperties.getReplication().getDelayThreshold());
    }

    private boolean changesAreMissing(ReplicationInfo record) {
        return (record.missingChanges > this.ldapProperties.getReplication().getMissingUpdatesThreshold());
    }

    @EqualsAndHashCode
    static class ReplicationInfo {
        ReplicationRecordType recordType = ReplicationRecordType.UNKNOWN;
        String dn;
        String connectedReplicationServer;
        String connectedDirectoryServer;
        String directoryServer;
        String replicationServer;
        String status;
        Integer pendingUpdates;
        Integer missingChanges;
        Integer currentDelay;
        Integer sentUpdates;
        Integer receivedUpdates;
        Integer replayedUpdates;

        @Override
        public String toString() {
            List<String> display = new ArrayList<>();
            if (replicationServer != null) {
                display.add("rs:" + replicationServer);
            }
            if (directoryServer != null) {
                display.add("ds:" + directoryServer);
            }
            if (connectedReplicationServer != null) {
                display.add("connected-rs:" + connectedReplicationServer);
            }
            if (connectedDirectoryServer != null) {
                display.add("connected-ds:" + connectedDirectoryServer);
            }
            if (display.isEmpty()) {
                display.add("dn:" + dn);
            }
            display.add("status:" + status);
            display.add("pending:" + pendingUpdates);
            display.add("missing:" + missingChanges);
            display.add("sent:" + sentUpdates);
            display.add("received:" + receivedUpdates);
            display.add("replayed:" + replayedUpdates);
            display.add("delay:" + currentDelay);
            return String.join(",", display);
        }
    }

    enum ReplicationRecordType {
        LOCAL_RS,
        LOCAL_DS,
        LOCAL_RS_CONN_DS,
        REMOTE_CONN_RS,
        REMOTE_CONN_RS_CONN_DS,
        UNKNOWN;
    }

    static class ReplicationContextMapper implements ContextMapper<ReplicationInfo> {

        @Override
        public ReplicationInfo mapFromContext(Object ctx) {
            DirContextAdapter context = (DirContextAdapter) ctx;
            Name dn = context.getDn();

            log.debug(context.toString());

            String statusAttribute = context.getStringAttribute(STATUS_ATTRIBUTE);
            String pendingUpdatesAttribute = context.getStringAttribute(PENDING_UPDATES_ATTRIBUTE);
            String missingChangesAttribute = context.getStringAttribute(MISSING_CHANGES_ATTRIBUTE);
            String currentDelay = context.getStringAttribute(CURRENT_DELAY);
            String sentUpdates = context.getStringAttribute(SENT_UPDATES_ATTRIBUTE);
            String receivedUpdates = context.getStringAttribute(RECEIVED_UPDATES_ATTRIBUTE);
            String replayedUpdates = context.getStringAttribute(REPLAYED_UPDATES_ATTRIBUTE);

            String connectedReplicationServer = null;
            String connectedDirectoryServer = null;
            String replicationServer = null;
            String directoryServer = null;
            for (Enumeration<String> e = dn.getAll(); e.hasMoreElements(); ) {
                String value = e.nextElement();
                if (value.startsWith("cn=Connected replication server")) {
                    connectedReplicationServer = value.split("\\) ")[1];
                } else if (value.startsWith("cn=Connected directory server")) {
                    connectedDirectoryServer = value.split("\\) ")[1];
                } else if (value.startsWith("cn=Directory server")) {
                    directoryServer = value.split("\\) ")[1];
                } else if (value.startsWith("cn=Replication server")) {
                    replicationServer = value.split("\\) ")[1];
                }
            }

            ReplicationInfo result = new ReplicationInfo();
            result.dn = dn.toString();
            result.connectedReplicationServer = connectedReplicationServer;
            result.connectedDirectoryServer = connectedDirectoryServer;
            result.directoryServer = directoryServer;
            result.replicationServer = replicationServer;
            result.status = statusAttribute;
            result.pendingUpdates = toInt(pendingUpdatesAttribute);
            result.missingChanges = toInt(missingChangesAttribute);
            result.currentDelay = toInt(currentDelay);
            result.sentUpdates = toInt(sentUpdates);
            result.receivedUpdates = toInt(receivedUpdates);
            result.replayedUpdates = parseReplayedUpdates(replayedUpdates);
            result.recordType = getType(replicationServer, directoryServer, connectedReplicationServer, connectedDirectoryServer);

            return result;
        }

        static int parseReplayedUpdates(@Nullable String replayedUpdates) {
            if (replayedUpdates != null) {
                try {
                    return new JsonParser().parse(replayedUpdates).getAsJsonObject().get("count").getAsInt();
                } catch (JsonParseException | IllegalStateException e) {
                    log.error("JSON expected but got this: {} [{}]", replayedUpdates, e.getMessage());
                }
            }
            return -1;
        }

        Integer toInt(String value) {
            try {
                return value == null ? -1 : Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                log.error("NumberFormatException caused by: " + value);
                return -1;
            }
        }

        ReplicationRecordType getType(String replServer, String dirServer, String connectedReplServer, String connectedDirServer) {
            if (StringUtils.isNoneEmpty(connectedReplServer, connectedDirServer)) {
                return ReplicationRecordType.REMOTE_CONN_RS_CONN_DS;
            } else if (StringUtils.isNotEmpty(connectedReplServer)) {
                return ReplicationRecordType.REMOTE_CONN_RS;
            } else if ((StringUtils.isNotEmpty(connectedDirServer)) && (StringUtils.isNotEmpty(replServer))) {
                return ReplicationRecordType.LOCAL_RS_CONN_DS;
            } else if (StringUtils.isNoneEmpty(replServer, dirServer)) {
                return ReplicationRecordType.UNKNOWN;
            } else if (StringUtils.isNotEmpty(replServer)) {
                return ReplicationRecordType.LOCAL_RS;
            } else if (StringUtils.isNotEmpty(dirServer)) {
                return ReplicationRecordType.LOCAL_DS;
            } else {
                return ReplicationRecordType.UNKNOWN;
            }
        }
    }
}
