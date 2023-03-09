package uk.gov.hmcts.reform.idam.health.command;

import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Profile({"(userstore | tokenstore) & replication)"})
@CustomLog
public class ReplicationCommandProbe extends HealthProbe {

    private static final String SPACE = " ";
    private static final String RESULT_DELIM = "\t";
    private static final String REFORM_HMCTS_NET = "dc=reform,dc=hmcts,dc=net";

    private final ReplicationCommandProbeProperties probeProperties;
    private final TextCommandRunner textCommandRunner;

    private String[] command;

    public ReplicationCommandProbe(ReplicationCommandProbeProperties probeProperties, TextCommandRunner textCommandRunner) {
        this.probeProperties = probeProperties;
        this.textCommandRunner = textCommandRunner;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean probe() {
        try {
            ReplicationStatus status = run(getCommand());
            if (status.getHostReplicationInfo() != null) {

                boolean result = true;
                log.info("{}: Host replication info: {}", getName(), status.getHostReplicationInfo());
                if (CollectionUtils.isNotEmpty(status.getReplicationInfoList())) {
                    status.getReplicationInfoList().stream().forEach(ri -> log.info("{}: Replicated host: {}", getName(), ri));
                    if (!verifyHostReplication(status.getHostReplicationInfo())) {
                        result = false;
                    }
                    if (!compareReplication(status.getHostReplicationInfo(), status.getReplicationInfoList())) {
                        result = false;
                    }
                } else {
                    result = verifyHostReplication(status.getHostReplicationInfo());
                }

                if (result) {
                    return handleSuccess();
                } else {
                    return handleError("Host replication failed verification");
                }

            } else if ((CollectionUtils.isNotEmpty(status.getReplicationInfoList()))) {
                status.getReplicationInfoList().stream().forEach(ri -> log.info("{}: {}", getName(), ri));
                if (CollectionUtils.isNotEmpty(status.getErrors())) {
                    return handleError(String.join(", ", status.getErrors()));
                }
            } else if (CollectionUtils.isNotEmpty(status.getErrors())) {
                return handleError(String.join(", ", status.getErrors()));
            } else {
                return handleError("Command completed with no replication info present");
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return handleError("Failed");
    }

    @Override
    public String getName() {
        return probeProperties.getCommand().getName();
    }

    public String[] getCommand() {
        if (command == null) {
            command = buildCommand(probeProperties.getCommand().getTemplate(), probeProperties.getCommand().getUser(), probeProperties.getCommand().getPassword());
        }
        return command;
    }

    protected boolean verifyHostReplication(ReplicationInfo replicationInfo) {
        if ((probeProperties.getCommand().getDelayThreshold() != null) &&
                (replicationInfo.getDelay() != null) &&
                (replicationInfo.getDelay() > probeProperties.getCommand().getDelayThreshold())) {
            return false;
        }
        return true;
    }

    private boolean compareReplication(ReplicationInfo hostReplicationInfo, List<ReplicationInfo> replicationInfoList) {
        if ((probeProperties.getCommand().getEntryDifferenceThreshold() != null) &&
                (hostReplicationInfo.getEntries() != null)) {
            Long maxNoEntries = replicationInfoList.stream()
                    .max(Comparator.comparingLong(ReplicationInfo::getEntries)).get().getEntries();
            return (maxNoEntries == null) ||
                    (hostReplicationInfo.getEntries() >= maxNoEntries) ||
                    (hostReplicationInfo.getEntries() >= maxNoEntries - probeProperties.getCommand().getEntryDifferenceThreshold());
        }
        return true;
    }

    protected ReplicationStatus run(String[] command) throws InterruptedException, ExecutionException, IOException {
        log.debug("Pulling replication command response...");
        TextCommandRunner.Response response = textCommandRunner.execute(command, probeProperties.getCommand().getCommandTimeout());
        ReplicationStatus status = new ReplicationStatus();
        if (CollectionUtils.isNotEmpty(response.getOutput())) {
            for (String value : response.getOutput()) {
                log.debug("Response value: {}", value);
                if (value.startsWith(REFORM_HMCTS_NET)) {
                    ReplicationInfo info = convert(value);
                    if (info != null) {
                        if ((StringUtils.isNotEmpty(probeProperties.getCommand().getHostIdentity()) &&
                                StringUtils.startsWith(info.getHostName(), probeProperties.getCommand().getHostIdentity()))) {
                            status.setHostReplicationInfo(info);
                        } else {
                            status.getReplicationInfoList().add(info);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(response.getErrors())) {
            for (String error : response.getErrors()) {
                status.getErrors().add(error);
            }
        }
        return status;
    }

    protected ReplicationInfo convert(String value) {
        String[] parts = value.split(RESULT_DELIM);
        if (parts.length == 9) {
            ReplicationInfo info = new ReplicationInfo();
            info.setSuffix(StringUtils.trimToNull(parts[0]));
            info.setHostName(StringUtils.trimToNull(parts[1]));
            String entries = StringUtils.trimToNull(parts[2]);
            if (entries != null) {
                info.setEntries(Long.parseLong(entries));
            } else {
                info.setEntries(-1L);
            }
            info.setReplicationEnabled(StringUtils.trimToNull(parts[3]));
            info.setDsID(StringUtils.trimToNull(parts[4]));
            info.setRsId(StringUtils.trimToNull(parts[5]));
            info.setRsPort(StringUtils.trimToNull(parts[6]));
            String delay = StringUtils.trimToNull(parts[7]);
            if (delay != null) {
                info.setDelay(delay.equals("N/A") ? 0 : Long.parseLong(delay));
            }
            info.setSecurityEnabled(StringUtils.trimToNull(parts[8]));
            return info;
        }
        return null;
    }

    protected static String[] buildCommand(String commandTemplate, String adminUID, String adminPassword) {
        if (StringUtils.isNoneEmpty(commandTemplate, adminUID, adminPassword)) {
            log.info("Configuring with command {} and password value from properties", commandTemplate);
            return String.format(commandTemplate, adminUID, adminPassword).split(SPACE);
        } else if (commandTemplate != null) {
            if (StringUtils.isEmpty(adminPassword)) {
                log.warn("No value for admin password");
            }
            if (StringUtils.isEmpty(adminUID)) {
                log.warn("No value for admin user ID");
            }
            log.info("Configuring with command {}", commandTemplate);
            return commandTemplate.split(SPACE);
        }
        log.warn("command template is null");
        return null;
    }

}
