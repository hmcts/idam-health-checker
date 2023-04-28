package uk.gov.hmcts.reform.idam.health.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setup() {
        when(probeProperties.getCommand().getUser()).thenReturn("test-user");
        when(probeProperties.getCommand().getDSUPassword()).thenReturn("test-password");
        when(probeProperties.getCommand().getTemplate()).thenReturn("test-template %s %s");
        when(probeProperties.getCommand().getReplicationIdentity()).thenReturn("test-identity");
        when(probeProperties.getCommand().getCommandTimeout()).thenReturn(20000L);
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getDelayThreshold()).thenReturn(10L);
        when(probeProperties.getCommand().getEntryDifferencePercent()).thenReturn(0.2);
        when(probeProperties.getCommand().getUser()).thenReturn("test-user");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"userstore"});
    }

    @Test
    public void testVerify_PrimaryHostOkay() {
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        status.getContextReplicationInfo().get("test-context").add(simpleReplicationInfo("test-context", "test-instance", InstanceType.PRIMARY));
        boolean result = probe.verify(status);
        assertThat(result, is(true));
    }

    @Test
    public void testVerify_PrimaryHostNotPresent() {
        ReplicationStatus status = new ReplicationStatus();
        status.getContextReplicationInfo().put("test-context", new ArrayList<>());
        status.getContextReplicationInfo().get("test-context").add(simpleReplicationInfo("test-context", "test-instance", InstanceType.REPLICA));
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testVerify_PrimaryHostHasDelay() {
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
        ReplicationStatus status = new ReplicationStatus();
        status.setErrors(Collections.singletonList("test-error"));
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testVerify_StatusEmpty() {
        ReplicationStatus status = new ReplicationStatus();
        boolean result = probe.verify(status);
        assertThat(result, is(false));
    }

    @Test
    public void testProbe_Success() throws IOException, ExecutionException, InterruptedException {
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
        when(textCommandRunner.execute(any(), any())).thenThrow(new RuntimeException("test-exception"));
        boolean result = probe.probe();
        assertThat(result, is(false));
    }

    @Test
    public void testGetCommand_Success() {
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
