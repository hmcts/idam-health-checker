package uk.gov.hmcts.reform.idam.health.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
@Profile({"(userstore | tokenstore) & replication & check-ready)"})
public class TextCommandRunner {

    private final AsyncTaskExecutor taskExecutor;

    public TextCommandRunner(AsyncTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public Response execute(String[] command, Long commandTimeoutMillis) throws IOException, InterruptedException, ExecutionException {
        Process commandProcess = new ProcessBuilder().command(command).start();
        Future<List<String>> output = taskExecutor.submit(new TextReceiver(commandProcess.getInputStream()));
        Future<List<String>> errors = taskExecutor.submit(new TextReceiver(commandProcess.getErrorStream()));
        if (!commandProcess.waitFor(commandTimeoutMillis, TimeUnit.MILLISECONDS)) {
            commandProcess.destroy();
            commandProcess.waitFor();
            output.cancel(true);
            errors.cancel(true);
            throw new InterruptedException("Command timed out");
        }
        return new Response(output.get(), errors.get());
    }

    private class TextReceiver implements Callable<List<String>> {

        private final InputStream inputStream;

        private TextReceiver(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public List<String> call() throws Exception {
            List<String> result = new ArrayList<>();
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(l -> result.add(l));
            return result;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Response {
        private List<String> output;
        private List<String> errors;
    }
}
