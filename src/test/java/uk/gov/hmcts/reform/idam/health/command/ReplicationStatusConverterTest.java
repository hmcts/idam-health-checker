package uk.gov.hmcts.reform.idam.health.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationStatusConverterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReplicationCommandProbeProperties probeProperties;

    @InjectMocks
    private ReplicationStatusConverter replicationStatusConverter;

    @Before
    public void setup() {
        when(probeProperties.getCommand().getReplicationIdentity()).thenReturn("test-identity");
    }

    @Test
    public void convert_withInfo() {
        List<String> textOutput = new ArrayList<>();
        textOutput.add("line1");
        textOutput.add("line2");
        textOutput.add("line3");
        textOutput.add("dc=test-context");
        textOutput.add("├ test-identity test-status 1 2 3 test-hostname");
        textOutput.add("└ test-other-identity test-other-status 4 5 6 test-other-hostname");
        textOutput.add("ou=test-other-context");
        textOutput.add("├ test-identity test-status 9 8 7 test-hostname");
        textOutput.add("└ test-other-identity test-other-status 6 5 4 test-other-hostname");
        TextCommandRunner.Response response = new TextCommandRunner.Response(textOutput, Collections.emptyList());
        ReplicationStatus status = replicationStatusConverter.convert(response);
        assertThat(status.getContextReplicationInfo().size(), is(2));
        List<ReplicationInfo> results = status.getContextReplicationInfo().get("dc=test-context");
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getInstance(), is("test-identity"));
        assertThat(results.get(0).getInstanceType(), is(InstanceType.PRIMARY));
        assertThat(results.get(0).getStatus(), is("test-status"));
        assertThat(results.get(0).getReceiveDelayMs(), is (1L));
        assertThat(results.get(0).getReplayDelayMs(), is(2L));
        assertThat(results.get(0).getEntryCount(), is(3L));
        assertThat(results.get(1).getInstance(), is("test-other-identity"));
        assertThat(results.get(1).getInstanceType(), is(InstanceType.REPLICA));
        assertThat(results.get(1).getStatus(), is("test-other-status"));
        assertThat(results.get(1).getReceiveDelayMs(), is (4L));
        assertThat(results.get(1).getReplayDelayMs(), is(5L));
        assertThat(results.get(1).getEntryCount(), is(6L));
        List<ReplicationInfo> otherResults = status.getContextReplicationInfo().get("ou=test-other-context");
        assertThat(otherResults.size(), is(2));
        assertThat(otherResults.get(0).getInstance(), is("test-identity"));
        assertThat(otherResults.get(0).getInstanceType(), is(InstanceType.PRIMARY));
        assertThat(otherResults.get(0).getStatus(), is("test-status"));
        assertThat(otherResults.get(0).getReceiveDelayMs(), is (9L));
        assertThat(otherResults.get(0).getReplayDelayMs(), is(8L));
        assertThat(otherResults.get(0).getEntryCount(), is(7L));
        assertThat(otherResults.get(1).getInstance(), is("test-other-identity"));
        assertThat(otherResults.get(1).getInstanceType(), is(InstanceType.REPLICA));
        assertThat(otherResults.get(1).getStatus(), is("test-other-status"));
        assertThat(otherResults.get(1).getReceiveDelayMs(), is (6L));
        assertThat(otherResults.get(1).getReplayDelayMs(), is(5L));
        assertThat(otherResults.get(1).getEntryCount(), is(4L));
    }

    @Test
    public void convert_withParsingProblems() {
        List<String> textOutput = new ArrayList<>();
        textOutput.add("line1");
        textOutput.add("line2");
        textOutput.add("line3");
        textOutput.add("dc=test-context");
        textOutput.add("├ fail");
        textOutput.add("├ test-identity test-status 1 2 3 test-hostname");
        textOutput.add("└ test-other-identity test-other-status fail 5 6 test-other-hostname");
        TextCommandRunner.Response response = new TextCommandRunner.Response(textOutput, Collections.emptyList());
        ReplicationStatus status = replicationStatusConverter.convert(response);
        assertThat(status.getContextReplicationInfo().size(), is(1));
        List<ReplicationInfo> results = status.getContextReplicationInfo().get("dc=test-context");
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getInstance(), is("test-identity"));
        assertThat(results.get(0).getInstanceType(), is(InstanceType.PRIMARY));
        assertThat(results.get(0).getStatus(), is("test-status"));
        assertThat(results.get(0).getReceiveDelayMs(), is (1L));
        assertThat(results.get(0).getReplayDelayMs(), is(2L));
        assertThat(results.get(0).getEntryCount(), is(3L));
    }

    @Test
    public void convert_withDs8HostnameColumn() {
        when(probeProperties.getCommand().getReplicationIdentity()).thenReturn("forgerock_ds_tokenstore_idam_sandbox_2");
        List<String> textOutput = new ArrayList<>();
        textOutput.add("Base DN / DS                                  Status  Receive     Replay      Entry count  Hostname");
        textOutput.add("                                                      delay (ms)  delay (ms)");
        textOutput.add("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        textOutput.add("ou=tokens");
        textOutput.add("├─ DS/forgerock_ds_tokenstore_idam_sandbox_1  GOOD             0           0         3038  forgerock-ds-tokenstore-idam-sandbox-1.service.core-compute-idam-sandbox.internal");
        textOutput.add("├─ DS/forgerock_ds_tokenstore_idam_sandbox_2  GOOD             0           0         3038  forgerock-ds-tokenstore-idam-sandbox-2.service.core-compute-idam-sandbox.internal");
        textOutput.add("└─ DS/forgerock_ds_tokenstore_idam_sandbox_3  GOOD             0           0         3038  forgerock-ds-tokenstore-idam-sandbox-3.service.core-compute-idam-sandbox.internal");
        TextCommandRunner.Response response = new TextCommandRunner.Response(textOutput, Collections.emptyList());

        ReplicationStatus status = replicationStatusConverter.convert(response);

        assertThat(status.getContextReplicationInfo().size(), is(1));
        List<ReplicationInfo> results = status.getContextReplicationInfo().get("ou=tokens");
        assertThat(results.size(), is(3));
        assertThat(results.get(1).getInstance(), is("DS/forgerock_ds_tokenstore_idam_sandbox_2"));
        assertThat(results.get(1).getInstanceType(), is(InstanceType.PRIMARY));
        assertThat(results.get(1).getStatus(), is("GOOD"));
        assertThat(results.get(1).getReceiveDelayMs(), is(0L));
        assertThat(results.get(1).getReplayDelayMs(), is(0L));
        assertThat(results.get(1).getEntryCount(), is(3038L));
    }

    @Test
    public void convert_withErrors() {
        TextCommandRunner.Response response = new TextCommandRunner.Response(Collections.emptyList(), Collections.singletonList("test-error"));
        ReplicationStatus status = replicationStatusConverter.convert(response);
        assertThat(status.getContextReplicationInfo().isEmpty(), is(true));
        assertThat(status.getErrors().size(), is(1));
        assertThat(status.getErrors().get(0), is("test-error"));
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
