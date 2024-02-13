package uk.gov.hmcts.reform.idam.health.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.text.MessageFormat.format;

@Component
@Profile("(userstore | tokenstore) & replication)")
@Slf4j
public class ReplicationCommandProbe extends HealthProbe {

    private static final String SPACE = " ";
    private final ReplicationCommandProbeProperties probeProperties;
    private final TextCommandRunner textCommandRunner;
    private final Environment environment;
    private final ReplicationStatusConverter replicationStatusConverter;

    private String[] command;

    public ReplicationCommandProbe(ReplicationCommandProbeProperties probeProperties,
                                   TextCommandRunner textCommandRunner, Environment environment,
                                   ReplicationStatusConverter converter) {
        this.probeProperties = probeProperties;
        this.textCommandRunner = textCommandRunner;
        this.environment = environment;
        replicationStatusConverter = converter;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean probe() {
        try {
            ReplicationStatus status = run(getCommand());
            return verify(status);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    protected boolean verify(ReplicationStatus status) {
        if (MapUtils.isNotEmpty(status.getContextReplicationInfo())) {
            List<String> healthErrors = new ArrayList<>();
            boolean result = true;
            for (String context : status.getContextReplicationInfo().keySet()) {
                ReplicationInfo hostReplicationInfo = null;
                for (ReplicationInfo replicationInfo : status.getContextReplicationInfo()
                        .get(context)) {
                    if (replicationInfo.getInstanceType() == InstanceType.PRIMARY) {
                        hostReplicationInfo = replicationInfo;
                    }
                    log.info("{}", replicationInfo);
                }
                if (hostReplicationInfo == null) {
                    healthErrors.add(format("Context {0}: No primary host details for identity {1}", context,
                                            probeProperties.getCommand().getReplicationIdentity()));
                    result = false;
                } else {
                    if (!verifyHostReplication(hostReplicationInfo)) {
                        healthErrors.add(format("Context {0}: failed primary host verification", context));
                        result = false;
                    }
                    Long maxReplicaEntryCount = status.getContextReplicationInfo().get(context).stream()
                            .filter(s -> s.getInstanceType() != InstanceType.PRIMARY)
                            .max(Comparator.comparingLong(ReplicationInfo::getEntryCount))
                            .map(ReplicationInfo::getEntryCount).orElse(0L);
                    if (!compareReplication(hostReplicationInfo, maxReplicaEntryCount)) {
                        healthErrors.add(
                                format("Context {0}: primary host {1} with entry count of {2} failed verification against max replication count {3}",
                                       context, hostReplicationInfo.getInstance(), hostReplicationInfo.getEntryCount(),
                                       maxReplicaEntryCount));
                        result = false;
                    }
                }
            }
            if (result) {
                return handleSuccess();
            } else {
                return handleError(String.join("; ", healthErrors));
            }
        } else if (CollectionUtils.isNotEmpty(status.getErrors())) {
            return handleError(String.join(", ", status.getErrors()));
        } else {
            return handleError("Command completed with no replication info present");
        }
    }

    private boolean compareReplication(ReplicationInfo hostReplicationInfo, Long maxReplicaEntryCount) {
        if (hostReplicationInfo.getEntryCount() != null
                && hostReplicationInfo.getEntryCount() >= 0L
                && maxReplicaEntryCount >= 0L
                && probeProperties.getCommand().getEntryDifferencePercent() != null
                && hostReplicationInfo.getEntryCount() < maxReplicaEntryCount - (maxReplicaEntryCount * probeProperties.getCommand().getEntryDifferencePercent())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return probeProperties.getCommand().getName();
    }

    public String[] getCommand() {
        if (command == null) {
            command = buildCommand(probeProperties.getCommand().getTemplate(),
                                   probeProperties.getCommand().getUser(),
                                   getBindPassword());
        }
        return command;
    }

    protected boolean verifyHostReplication(ReplicationInfo replicationInfo) {
        if ((probeProperties.getCommand()
                .getDelayThreshold() != null) && (replicationInfo.getReceiveDelayMs() != null) && (replicationInfo.getReceiveDelayMs() > probeProperties.getCommand()
                .getDelayThreshold())) {
            return false;
        }
        return true;
    }

    protected ReplicationStatus run(String[] command) throws InterruptedException, ExecutionException, IOException {
        log.info("Running replication command");
        TextCommandRunner.Response response = textCommandRunner.execute(command, probeProperties.getCommand()
                .getCommandTimeout());
        return replicationStatusConverter.convert(response);
    }

    protected static String[] buildCommand(String commandTemplate, String adminUID, String adminPassword) {
        if (StringUtils.isNoneEmpty(commandTemplate, adminUID, adminPassword)) {
            return String.format(commandTemplate, adminUID, adminPassword).split(SPACE);
        } else if (commandTemplate != null) {
            if (StringUtils.isEmpty(adminPassword)) {
                log.warn("No value for admin password");
            }
            if (StringUtils.isEmpty(adminUID)) {
                log.warn("No value for admin user ID");
            }
            return commandTemplate.split(SPACE);
        }
        log.warn("command template is null");
        return null;
    }

    private String getBindPassword() {
        if (ArrayUtils.contains(environment.getActiveProfiles(), "userstore")) {
            return probeProperties.getCommand().getDSUPassword();
        } else {
            return probeProperties.getCommand().getDSTPassword();
        }
    }

}
