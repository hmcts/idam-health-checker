package uk.gov.hmcts.reform.idam.health.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReplicationCommandProbeTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReplicationCommandProbeProperties probeProperties;

    @Mock
    private TextCommandRunner textCommandRunner;

    @Mock
    private Environment environment;

    @Mock
    private ReplicationStatusConverter replicationStatusConverter;

    @InjectMocks
    private ReplicationCommandProbe probe;

    @Test
    public void testVerify_PrimaryHostOkay() {
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getDelayThreshold()).thenReturn(10L);
        when(probeProperties.getCommand().getEntryDifferencePercent()).thenReturn(0.2);
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        status.getContextReplicationInfo().get("test-context").add(simpleReplicationInfo("test-context", "test-instance", InstanceType.PRIMARY));
        boolean result = probe.verify(status);
        assertThat(result, is(true));
    }

    @Test
    public void testVerify_PrimaryHostNotPresent() {
        when(probeProperties.getCommand().getReplicationIdentity()).thenReturn("test-identity");
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        status.getContextReplicationInfo().get("test-context").add(simpleReplicationInfo("test-context", "test-instance", InstanceType.REPLICA));
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testVerify_PrimaryHostHasDelay() {
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getDelayThreshold()).thenReturn(10L);
        when(probeProperties.getCommand().getEntryDifferencePercent()).thenReturn(0.2);
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        ReplicationInfo hostInfo = simpleReplicationInfo("test-context", "test-instance", InstanceType.PRIMARY);
        hostInfo.setReceiveDelayMs(100L);
        status.getContextReplicationInfo().get("test-context").add(hostInfo);
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testVerify_PrimaryHostMissingEntries() {
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getDelayThreshold()).thenReturn(10L);
        when(probeProperties.getCommand().getEntryDifferencePercent()).thenReturn(0.2);
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        status.getContextReplicationInfo().get("test-context").add(simpleReplicationInfo("test-context", "test-instance", InstanceType.PRIMARY));
        ReplicationInfo replicaInfo = simpleReplicationInfo("test-context", "test-instance", InstanceType.REPLICA);
        replicaInfo.setEntryCount(150L);
        status.getContextReplicationInfo().get("test-context").add(replicaInfo);
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testVerify_PrimaryHostMissingEntriesWithinThreshold() {
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getDelayThreshold()).thenReturn(10L);
        when(probeProperties.getCommand().getEntryDifferencePercent()).thenReturn(0.2);
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        status.getContextReplicationInfo().get("test-context").add(simpleReplicationInfo("test-context", "test-instance", InstanceType.PRIMARY));
        ReplicationInfo replicaInfo = simpleReplicationInfo("test-context", "test-instance", InstanceType.REPLICA);
        replicaInfo.setEntryCount(125L);
        status.getContextReplicationInfo().get("test-context").add(replicaInfo);
        boolean result = probe.verify(status);
        assertThat(result, is(true));
    }

    @Test
    public void testVerify_StatusErrors() {
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        ReplicationStatus status = new ReplicationStatus();
        status.setErrors(Collections.singletonList("test-error"));
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testVerify_StatusEmpty() {
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        ReplicationStatus status = new ReplicationStatus();
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testProbe_Success() throws IOException, ExecutionException, InterruptedException {
        when(probeProperties.getCommand().getDSUPassword()).thenReturn("test-password");
        when(probeProperties.getCommand().getTemplate()).thenReturn("test-template %s %s");
        when(probeProperties.getCommand().getCommandTimeout()).thenReturn(20000L);
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getDelayThreshold()).thenReturn(10L);
        when(probeProperties.getCommand().getEntryDifferencePercent()).thenReturn(0.2);
        when(probeProperties.getCommand().getUser()).thenReturn("test-user");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"userstore"});
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        status.getContextReplicationInfo().get("test-context").add(simpleReplicationInfo("test-context", "test-instance", InstanceType.PRIMARY));
        when(textCommandRunner.execute(any(), any())).thenReturn(new TextCommandRunner.Response(Collections.singletonList("output"), Collections.emptyList()));
        when(replicationStatusConverter.convert(any())).thenReturn(status);
        boolean result = probe.probe();
        assertThat(result, is(true));
    }

    @Test
    public void testProbe_Exception() throws IOException, ExecutionException, InterruptedException {
        when(probeProperties.getCommand().getDSUPassword()).thenReturn("test-password");
        when(probeProperties.getCommand().getTemplate()).thenReturn("test-template %s %s");
        when(probeProperties.getCommand().getCommandTimeout()).thenReturn(20000L);
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getUser()).thenReturn("test-user");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"userstore"});
        when(textCommandRunner.execute(any(), any())).thenThrow(new RuntimeException("test-exception"));
        boolean result = probe.probe();
        assertThat(result, is(false));
    }

    @Test
    public void testGetCommand_Success() {
        when(probeProperties.getCommand().getDSUPassword()).thenReturn("test-password");
        when(probeProperties.getCommand().getTemplate()).thenReturn("test-template %s %s");
        when(probeProperties.getCommand().getUser()).thenReturn("test-user");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"userstore"});
        String[] result = probe.getCommand();
        assertThat(result.length, is(3));
        assertThat(result[0], is("test-template"));
        assertThat(result[1], is("test-user"));
        assertThat(result[2], is("test-password"));
    }

    private ReplicationInfo simpleReplicationInfo(String context, String instance, InstanceType instanceType) {
        ReplicationInfo info = new ReplicationInfo();
        info.setContext(context);
        info.setInstance(instance);
        info.setInstanceType(instanceType);
        info.setEntryCount(100L);
        info.setStatus("test-okay");
        info.setReceiveDelayMs(0L);
        info.setReplayDelayMs(0L);
        return info;
    }

}
