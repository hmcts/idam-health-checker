package uk.gov.hmcts.reform.idam.health.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReplicationCommandProbe extends CommandProbe {

    private static final String SPACE = " ";
    private static final String RESULT_DELIM = "\t";
    private static final String REFORM_HMCTS_NET = "dc=reform,dc=hmcts,dc=net";

    public ReplicationCommandProbe(String probeName, String commandTemplate, String adminPassword, String hostname) {
        super(probeName, buildCommand(commandTemplate, adminPassword, hostname));
    }

    @Override
    protected boolean handleResponse(List<String> response) {
        if (CollectionUtils.isNotEmpty(response)) {
            List<ReplicationInfo> replicationInfoList = convert(response);
            if (CollectionUtils.isNotEmpty(replicationInfoList)) {
                for (ReplicationInfo replicationInfo : replicationInfoList) {
                    log.info("{}: {}", getName(), replicationInfo);
                }
            }
        }
        return true;
    }

    protected static String[] buildCommand(String commandTemplate, String adminPassword, String hostname) {
        return String.format(commandTemplate, adminPassword, hostname).split(SPACE);
    }

    protected List<ReplicationInfo> convert(List<String> input) {
        List<ReplicationInfo> result = new ArrayList<>();
        for (String value : input) {
            if (value.startsWith(REFORM_HMCTS_NET)) {
                ReplicationInfo converted = convert(value);
                if (converted != null) {
                    result.add(converted);
                }
            }
        }
        return result;
    }

    protected ReplicationInfo convert(String value) {
        String[] parts = value.split(RESULT_DELIM);
        if (parts.length == 10) {
            ReplicationInfo info = new ReplicationInfo();
            int i = 0;
            info.suffix = StringUtils.trimToNull(parts[i++]);
            info.hostName = StringUtils.trimToNull(parts[i++]);
            info.entries = StringUtils.trimToNull(parts[i++]);
            info.replicationEnabled = StringUtils.trimToNull(parts[i++]);
            info.dsID = StringUtils.trimToNull(parts[i++]);
            info.rsId = StringUtils.trimToNull(parts[i++]);
            info.rsPort = StringUtils.trimToNull(parts[i++]);
            info.missingChanges = StringUtils.trimToNull(parts[i++]);
            info.ageOfMissingChanges = StringUtils.trimToNull(parts[i++]);
            info.securityEnabled = StringUtils.trimToNull(parts[i++]);
            return info;
        }
        return null;
    }

    public class ReplicationInfo {
        String suffix;
        String hostName;
        String entries;
        String replicationEnabled;
        String dsID;
        String rsId;
        String rsPort;
        String missingChanges;
        String ageOfMissingChanges;
        String securityEnabled;

        @Override
        public String toString() {
            return "ReplicationInfo{" +
                    "suffix='" + suffix + '\'' +
                    ", hostName='" + hostName + '\'' +
                    ", entries='" + entries + '\'' +
                    ", replicationEnabled='" + replicationEnabled + '\'' +
                    ", dsID='" + dsID + '\'' +
                    ", rsId='" + rsId + '\'' +
                    ", rsPort='" + rsPort + '\'' +
                    ", missingChanges='" + missingChanges + '\'' +
                    ", ageOfMissingChanges='" + ageOfMissingChanges + '\'' +
                    ", securityEnabled='" + securityEnabled + '\'' +
                    '}';
        }
    }

}
