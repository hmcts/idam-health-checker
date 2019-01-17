package uk.gov.hmcts.reform.idam.health.command;

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ReplicationCommandProbe implements HealthProbe {

    private static final String SPACE = " ";
    private static final String RESULT_DELIM = "\t";
    private static final String REFORM_HMCTS_NET = "dc=reform,dc=hmcts,dc=net";

    private final String probeName;
    private final String[] command;
    private final String hostIdentity;
    private final Long missingUpdatesThreshold;

    private CommandRunner runner = new CommandRunner();

    public ReplicationCommandProbe(String probeName, String commandTemplate, String adminPassword, String hostIdentity, Long missingUpdatesThreshold) {
        this.probeName = probeName;
        this.command = buildCommand(commandTemplate, adminPassword);
        this.hostIdentity = hostIdentity;
        this.missingUpdatesThreshold = missingUpdatesThreshold;
    }

    @Override
    public boolean probe() {
        try {
            ReplicationStatus status = run(command);
            if (status.getHostReplicationInfo() != null) {

                boolean result = true;
                log.info("{}: Host replication info: {}", getName(), status.getHostReplicationInfo());
                if (CollectionUtils.isNotEmpty(status.getReplicationInfoList())) {
                    // TODO compare host against others
                    status.getReplicationInfoList().stream().forEach(ri -> log.info("{}: Replicated host: {}", getName(), ri));
                } else {
                    result = verifyHostReplication(status.getHostReplicationInfo());
                }
                return result;

            } else if ((CollectionUtils.isNotEmpty(status.getReplicationInfoList()))) {
                status.getReplicationInfoList().stream().forEach(ri -> log.info("{}: {}", getName(), ri));
                if (CollectionUtils.isNotEmpty(status.getErrors())) {
                    log.warn("{}: {}", getName(), String.join(", ", status.getErrors()));
                }
            } else if (CollectionUtils.isNotEmpty(status.getErrors())) {
                log.error("{}: {}", getName(), String.join(", ", status.getErrors()));
            } else {
                log.error("Command completed with no replication info present");
            }
        } catch (Exception e) {
            log.error("{}: {} [{}]", getName(), e.getMessage(), e.getClass().getSimpleName());
        }
        return false;
    }

    @Override
    public String getName() {
        return probeName;
    }

    protected boolean verifyHostReplication(ReplicationInfo replicationInfo) {
        if ((replicationInfo.getMissingChanges() != null) &&
                (replicationInfo.getMissingChanges() <= missingUpdatesThreshold)) {
            return false;
        }
        return true;
    }

    @VisibleForTesting
    void setCommandRunner(CommandRunner commandRunner) {
        this.runner = commandRunner;
    }

    protected ReplicationStatus run(String[] command) throws IOException, InterruptedException, ExecutionException {
        ReplicationStatus status = new ReplicationStatus();
        runner.run(command, new ReplicationStatusCommandListener(status, hostIdentity));
        return status;
    }

    protected static String[] buildCommand(String commandTemplate, String adminPassword) {
        if (StringUtils.isNoneEmpty(commandTemplate, adminPassword)) {
            log.info("Configuring with command {} and password value from properties", commandTemplate);
            return String.format(commandTemplate, adminPassword).split(SPACE);
        } else if (commandTemplate != null) {
            if (StringUtils.isEmpty(adminPassword)) {
                log.warn("No value for admin password");
            }
            log.info("Configuring with command {}", commandTemplate);
            return commandTemplate.split(SPACE);
        }
        log.warn("command template is null");
        return null;
    }

    @AllArgsConstructor
    protected class ReplicationStatusCommandListener implements CommandRunner.CommandListener {

        private final ReplicationStatus status;
        private final String hostIdentity;

        @Override
        public void receive(String value) {
            if (value.startsWith(REFORM_HMCTS_NET)) {
                ReplicationInfo info = convert(value);
                if (info != null) {
                    if (StringUtils.startsWith(info.getHostName(), hostIdentity)) {
                        status.setHostReplicationInfo(info);
                    } else {
                        status.getReplicationInfoList().add(info);
                    }
                }
            }
        }

        @Override
        public void error(String value) {
            status.getErrors().add(value);
        }

        protected ReplicationInfo convert(String value) {
            String[] parts = value.split(RESULT_DELIM);
            if (parts.length == 10) {
                ReplicationInfo info = new ReplicationInfo();
                int i = 0;
                info.setSuffix(StringUtils.trimToNull(parts[i++]));
                info.setHostName(StringUtils.trimToNull(parts[i++]));
                info.setEntries(StringUtils.trimToNull(parts[i++]));
                info.setReplicationEnabled(StringUtils.trimToNull(parts[i++]));
                info.setDsID(StringUtils.trimToNull(parts[i++]));
                info.setRsId(StringUtils.trimToNull(parts[i++]));
                info.setRsPort(StringUtils.trimToNull(parts[i++]));
                String missingChanges = StringUtils.trimToNull(parts[i++]);
                if (missingChanges != null) {
                    info.setMissingChanges(Integer.parseInt(missingChanges));
                }
                info.setAgeOfMissingChanges(StringUtils.trimToNull(parts[i++]));
                info.setSecurityEnabled(StringUtils.trimToNull(parts[i++]));
                return info;
            }
            return null;
        }
    }

}
