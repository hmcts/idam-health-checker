package uk.gov.hmcts.reform.idam.health.command;

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.io.IOException;

@Slf4j
public class ReplicationCommandProbe implements HealthProbe {

    private static final String SPACE = " ";
    private static final String RESULT_DELIM = "\t";
    private static final String REFORM_HMCTS_NET = "dc=reform,dc=hmcts,dc=net";

    private final String probeName;
    private final String[] command;

    private CommandRunner runner = new CommandRunner();

    public ReplicationCommandProbe(String probeName, String commandTemplate, String adminPassword, String hostname) {
        this.probeName = probeName;
        this.command = buildCommand(commandTemplate, adminPassword, hostname);
    }

    @Override
    public boolean probe() {
        try {
            ReplicationStatus status = run(command);
            if (CollectionUtils.isNotEmpty(status.getReplicationInfoList())) {

                for (ReplicationInfo replicationInfo : status.getReplicationInfoList()) {
                    log.info("{}: {}", getName(), replicationInfo);
                }

                if (CollectionUtils.isNotEmpty(status.getErrors())) {
                    for (String message : status.getErrors()) {
                        log.warn("{}: {}", getName(), message);
                    }
                }

            } else if (CollectionUtils.isNotEmpty(status.getErrors())) {
                for (String message : status.getErrors()) {
                    log.error("{}: {}", getName(), message);
                }
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

    @VisibleForTesting
    void setCommandRunner(CommandRunner commandRunner) {
        this.runner = commandRunner;
    }

    protected ReplicationStatus run(String[] command) throws IOException, InterruptedException {
        ReplicationStatus status = new ReplicationStatus();
        runner.run(command, new ReplicationStatusCommandListener(status));
        return status;
    }

    protected static String[] buildCommand(String commandTemplate, String adminPassword, String hostname) {
        if (StringUtils.isNoneEmpty(commandTemplate, adminPassword, hostname)) {
            log.info("Configuring with command {} and values from properties", commandTemplate);
            return String.format(commandTemplate, adminPassword, hostname).split(SPACE);
        } else if (commandTemplate != null) {
            if (StringUtils.isEmpty(adminPassword)) {
                log.warn("No value for admin password");
            }
            if (StringUtils.isEmpty(hostname)) {
                log.warn("No value for hostname");
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

        @Override
        public void receive(String value) {
            if (value.startsWith(REFORM_HMCTS_NET)) {
                ReplicationInfo info = convert(value);
                if (info != null) {
                    status.getReplicationInfoList().add(info);
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
                info.setMissingChanges(StringUtils.trimToNull(parts[i++]));
                info.setAgeOfMissingChanges(StringUtils.trimToNull(parts[i++]));
                info.setSecurityEnabled(StringUtils.trimToNull(parts[i++]));
                return info;
            }
            return null;
        }
    }

}
