package uk.gov.hmcts.reform.idam.health.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("(userstore | tokenstore) & replication)")
@Slf4j
public class ReplicationStatusConverter {

    private static final List<String> PATH_STARTS = Arrays.asList("dc=", "ou=");
    private static final List<String> INFO_STARTS = Arrays.asList("├", "└");

    private final ReplicationCommandProbeProperties probeProperties;

    private ReplicationStatusConverter(ReplicationCommandProbeProperties probeProperties) {
        this.probeProperties = probeProperties;
    }

    public ReplicationStatus convert(TextCommandRunner.Response response) {
        ReplicationStatus status = new ReplicationStatus();
        if (CollectionUtils.isNotEmpty(response.getOutput()) && response.getOutput().size() > 3) {
            String context = null;
            for (int i = 3; i < response.getOutput().size(); i++) {
                String value = response.getOutput().get(i);
                log.info("Response value: {}", value);
                if (beginsWithAny(value, INFO_STARTS)) {
                    if (beginsWithAny(context, PATH_STARTS)) {
                        ReplicationInfo info = simpleConvert(value, context);
                        if (info != null) {
                            if (!status.getContextReplicationInfo().containsKey(context)) {
                                status.getContextReplicationInfo().put(context, new ArrayList<>());
                            }
                            status.getContextReplicationInfo().get(context).add(info);
                        }
                    }
                } else {
                    context = value.trim();
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

    private ReplicationInfo simpleConvert(String value, String context) {
        try {
            String[] parts = value.split("\\s+");
            if (parts.length == 6) {
                ReplicationInfo info = new ReplicationInfo();
                info.setInstance(parts[1]);
                info.setStatus(parts[2]);
                info.setReceiveDelayMs(safeParseLong(parts[3]));
                info.setReplayDelayMs(safeParseLong(parts[4]));
                info.setEntryCount(safeParseLong(parts[5]));
                info.setContext(context);
                if (info.getInstance().contains(probeProperties.getCommand().getReplicationIdentity())) {
                    info.setInstanceType(InstanceType.PRIMARY);
                } else {
                    info.setInstanceType(InstanceType.REPLICA);
                }
                return info;
            } else {
                log.warn("Context '{}': Unable to parse '{}', length was '{}'", context, value, parts.length);
            }
        } catch (Exception e) {
            log.error("Context '{}': Failed to parse '{}'", context, value, e);
        }
        return null;
    }

    private boolean beginsWithAny(String value, List<String> pathStarts) {
        for (String pathStart : pathStarts) {
            if (value.startsWith(pathStart)) {
                return true;
            }
        }
        return false;
    }

    private Long safeParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            log.warn("Failed to parse '{}' as Long", value, e);
        }
        return -1L;
    }

}
