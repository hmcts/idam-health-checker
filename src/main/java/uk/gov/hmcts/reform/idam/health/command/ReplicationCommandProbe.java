package uk.gov.hmcts.reform.idam.health.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;
import uk.gov.hmcts.reform.idam.health.props.ConfigProperties;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Profile({"userstore","tokenstore","replication"})
@Slf4j
public class ReplicationCommandProbe implements HealthProbe {

    private static final String SPACE = " ";
    private static final String RESULT_DELIM = "\t";
    private static final String REFORM_HMCTS_NET = "dc=reform,dc=hmcts,dc=net";

    private final ConfigProperties configProperties;
    private final ReplicationCommandProbeProperties probeProperties;
    private final TextCommandRunner textCommandRunner;

    private String[] command;

    public ReplicationCommandProbe(ConfigProperties configProperties, ReplicationCommandProbeProperties probeProperties, TextCommandRunner textCommandRunner) {
        this.configProperties = configProperties;
        this.probeProperties = probeProperties;
        this.textCommandRunner = textCommandRunner;
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
        return probeProperties.getCommand().getName();
    }

    public String[] getCommand() {
        if (command == null) {
            command = buildCommand(probeProperties.getCommand().getTemplate(), probeProperties.getCommand().getPassword());
        }
        return command;
    }

    protected boolean verifyHostReplication(ReplicationInfo replicationInfo) {
        if ((probeProperties.getCommand().getMissingUpdatesThreshold() != null) &&
                (replicationInfo.getMissingChanges() != null) &&
                (replicationInfo.getMissingChanges() > probeProperties.getCommand().getMissingUpdatesThreshold())) {
            return false;
        }
        return true;
    }

    private boolean compareReplication(ReplicationInfo hostReplicationInfo, List<ReplicationInfo> replicationInfoList) {
        if ((probeProperties.getCommand().getEntryDifferenceThreshold() != null) &&
                (hostReplicationInfo.getEntries() != null)) {
            Integer maxNoEntries = replicationInfoList.stream().max(Comparator.comparingInt(ReplicationInfo::getEntries)).get().getEntries();
            if ((maxNoEntries != null) &&
                    (hostReplicationInfo.getEntries() < maxNoEntries) &&
                    (hostReplicationInfo.getEntries() < maxNoEntries - probeProperties.getCommand().getEntryDifferenceThreshold())) {
                return false;
            }
        }
        return true;
    }

    protected ReplicationStatus run(String[] command) throws InterruptedException, ExecutionException, IOException {
        TextCommandRunner.Response response = textCommandRunner.execute(command, probeProperties.getCommand().getCommandTimeout());
        ReplicationStatus status = new ReplicationStatus();
        if (CollectionUtils.isNotEmpty(response.getOutput())) {
            for (String value : response.getOutput()) {
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
        if (parts.length == 10) {
            ReplicationInfo info = new ReplicationInfo();
            int i = 0;
            info.setSuffix(StringUtils.trimToNull(parts[i++]));
            info.setHostName(StringUtils.trimToNull(parts[i++]));
            String entries = StringUtils.trimToNull(parts[i++]);
            if (entries != null) {
                info.setEntries(Integer.parseInt(entries));
            } else {
                info.setEntries(-1);
            }
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

}
