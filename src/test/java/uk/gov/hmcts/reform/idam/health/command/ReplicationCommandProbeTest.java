package uk.gov.hmcts.reform.idam.health.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationCommandProbeTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReplicationCommandProbeProperties probeProperties;

    @Mock
    private TextCommandRunner textCommandRunner;

    @InjectMocks
    private ReplicationCommandProbe probe;

    @Before
    public void setup() {
        when(probeProperties.getCommand().getUser()).thenReturn("test-user");
        when(probeProperties.getCommand().getPassword()).thenReturn("test-password");
        when(probeProperties.getCommand().getTemplate()).thenReturn("test-template %s %s");
        when(probeProperties.getCommand().getHostIdentity()).thenReturn("test-host");
        when(probeProperties.getCommand().getCommandTimeout()).thenReturn(20000L);
        when(probeProperties.getCommand().getName()).thenReturn("test-probe");
        when(probeProperties.getCommand().getEntryDifferenceThreshold()).thenReturn(50L);
    }

    @Test
    public void testProbe_HostOkay() throws InterruptedException, ExecutionException, IOException {
        List<String> commandOutput = Collections.singletonList("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t27259\ttrue\t24501\t1265\t8989\t0\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(true));
    }

    @Test
    public void testProbe_HostMissingChanges() throws InterruptedException, ExecutionException, IOException {
        List<String> commandOutput = new ArrayList<>();
        commandOutput.add("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t27101\ttrue\t24501\t1265\t8989\t11\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-1:4444\t27259\ttrue\t24501\t1265\t8989\t11\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-2:4444\t27259\ttrue\t24501\t1265\t8989\t11\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(false));
    }

    @Test
    public void testProbe_HostOkayNoIdentity() throws InterruptedException, ExecutionException, IOException {
        when(probeProperties.getCommand().getHostIdentity()).thenReturn(null);
        List<String> commandOutput = Collections.singletonList("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t27259\ttrue\t24501\t1265\t8989\t0\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(false));
    }

    @Test
    public void testProbe_HostOkayAndOthersLagging() throws InterruptedException, ExecutionException, IOException {
        List<String> commandOutput = new ArrayList<>();
        commandOutput.add("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t27259\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-1:4444\t1000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-2:4444\t1000\ttrue\t24501\t1265\t8989\t0\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(false));
    }

    @Test
    public void testProbe_HostLaggingAndOthersOkay() throws InterruptedException, ExecutionException, IOException {
        List<String> commandOutput = new ArrayList<>();
        commandOutput.add("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t1000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-1:4444\t2000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-2:4444\t1500\ttrue\t24501\t1265\t8989\t0\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(false));
    }

    @Test
    public void testProbe_HostLaggingAndOthersSimilar() throws InterruptedException, ExecutionException, IOException {
        List<String> commandOutput = new ArrayList<>();
        commandOutput.add("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t1000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-1:4444\t1000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-2:4444\t1050\ttrue\t24501\t1265\t8989\t0\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(true));
    }

    @Test
    public void testProbe_HostOkayAndOthersMissingData() throws InterruptedException, ExecutionException, IOException {
        List<String> commandOutput = new ArrayList<>();
        commandOutput.add("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t1000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-1:4444\t\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-2:4444\t\ttrue\t24501\t1265\t8989\t0\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(false));
    }

    @Test
    public void testProbe_HostLaggingAndOthersOkayButNoThreshold() throws InterruptedException, ExecutionException, IOException {
        when(probeProperties.getCommand().getEntryDifferenceThreshold()).thenReturn(null);
        List<String> commandOutput = new ArrayList<>();
        commandOutput.add("dc=reform,dc=hmcts,dc=net\ttest-host:4444\t1000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-1:4444\t2000\ttrue\t24501\t1265\t8989\t0\ttrue");
        commandOutput.add("dc=reform,dc=hmcts,dc=net\tother-host-2:4444\t1500\ttrue\t24501\t1265\t8989\t0\ttrue");
        TextCommandRunner.Response testResponse = new TextCommandRunner.Response(commandOutput, null);
        when(textCommandRunner.execute(eq("test-template test-user test-password".split(" ")), eq(20000L))).thenReturn(testResponse);
        boolean result = probe.probe();
        assertThat(result, is(true));
    }

}
