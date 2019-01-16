package uk.gov.hmcts.reform.idam.health.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.reform.idam.health.probe.HealthProbe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
public class CommandProbe implements HealthProbe {

    private String probeName;
    private final String[] command;

    public CommandProbe(String probeName, String[] command) {
        this.probeName = probeName;
        this.command = command;
    }

    @Override
    public boolean probe() {
        try {
            List<String> result = executeCommand(getCommand());
            return handleResponse(result);
        } catch (IOException | InterruptedException e) {
            log.error("{}: {} [{}]", getName(), e.getMessage(), e.getClass().getSimpleName());
        }
        return false;
    }

    @Override
    public String getName() {
        return probeName;
    }

    protected boolean handleResponse(List<String> response) {
        if (CollectionUtils.isNotEmpty(response)) {
            log.info("Result: {}", String.join(", ", response));
            return true;
        }
        return false;
    }

    protected List<String> executeCommand(String[] command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        Process process = builder.start();
        List<String> commandOutput = new ArrayList<>();
        List<String> commandErrors = new ArrayList<>();
        StreamGobbler outputReceiver = new StreamGobbler(process.getInputStream(), s -> commandOutput.add(s));
        StreamGobbler errorReceiver = new StreamGobbler(process.getErrorStream(), s -> commandErrors.add(s));
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(outputReceiver);
        service.submit(errorReceiver);
        int exitCode = process.waitFor();
        assert exitCode == 0;
        if (CollectionUtils.isNotEmpty(commandOutput)) {
            return commandOutput;
        }
        if (CollectionUtils.isNotEmpty(commandErrors)) {
            log.error("{}: Error from command; {}", getName(), String.join(", ", commandErrors));
        }
        return null;
    }

    protected String[] getCommand() {
        return command;
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

}
