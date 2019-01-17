package uk.gov.hmcts.reform.idam.health.command;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationCommandProbeTest {

    @Mock
    private CommandRunner commandRunner;

    @Captor
    private ArgumentCaptor<CommandRunner.CommandListener> commandListener;

    @Test
    public void testProbe_HostOkay() throws InterruptedException, ExecutionException, IOException {
        ReplicationCommandProbe probe = new ReplicationCommandProbe("TestProbe", "command", "password", "id", null);

        // TODO refactor command runner so you can test this!

        probe.setCommandRunner(commandRunner);
        probe.probe();
        verify(commandRunner).run(any(), commandListener.capture());
        commandListener.getValue().error("Error");
    }

}
