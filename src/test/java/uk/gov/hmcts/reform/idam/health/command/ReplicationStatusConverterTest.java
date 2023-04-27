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
        textOutput.add("├ test-identity test-status 1 2 3");
        textOutput.add("└ test-other-identity test-other-status 4 5 6");
        textOutput.add("ou=test-other-context");
        textOutput.add("├ test-identity test-status 9 8 7");
        textOutput.add("└ test-other-identity test-other-status 6 5 4");
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
        textOutput.add("├ test-identity test-status 1 2 3");
        textOutput.add("└ test-other-identity test-other-status fail 5 6");
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