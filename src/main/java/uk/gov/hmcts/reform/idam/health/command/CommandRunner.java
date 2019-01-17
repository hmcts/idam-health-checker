package uk.gov.hmcts.reform.idam.health.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CommandRunner {

    private static final long DEFAULT_COMMAND_TIMEOUT = 10000L;

    private final long commandTimeout;

    private final static ExecutorService executor = Executors.newSingleThreadExecutor();

    public CommandRunner() {
        this(DEFAULT_COMMAND_TIMEOUT);
    }

    public CommandRunner(long commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    protected void run(String[] command, CommandListener listener) throws IOException, InterruptedException {
        Process commandProcess = startProcess(command);
        executor.submit(new StreamGobbler(commandProcess.getInputStream(), listener::receive));
        executor.submit(new StreamGobbler(commandProcess.getErrorStream(), listener::error));
        if (!commandProcess.waitFor(commandTimeout, TimeUnit.MILLISECONDS)) {
            commandProcess.destroy();
            commandProcess.waitFor();
            listener.error("Command timed out");
        }
    }

    protected Process startProcess(String[] command) throws IOException {
        return new ProcessBuilder().command(command).start();
    }

    public interface CommandListener {
        void receive(String value);
        void error(String value);
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
