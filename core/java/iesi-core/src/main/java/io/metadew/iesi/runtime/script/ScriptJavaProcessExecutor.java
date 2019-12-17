package io.metadew.iesi.runtime.script;

import io.metadew.iesi.metadata.configuration.script.exception.ScriptDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.metadata.definition.script.Script;
import io.metadew.iesi.metadata.service.execution.script.ScriptExecutionRequestHandlerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScriptJavaProcessExecutor extends ScriptExecutor {

    private final int timeout;
    private final Path home;

    public ScriptJavaProcessExecutor(int threadSize, Path path, int timeout) {
        super(threadSize);
        this.home = path.resolve("bin");
        this.timeout = timeout;
    }

    public ScriptJavaProcessExecutor(int threadSize, Path path) {
        this(threadSize, path, 60);
    }

    public void execute(ScriptExecutionRequest scriptExecutionRequest) throws IOException, InterruptedException, ScriptDoesNotExistException {
        ProcessBuilder builder = new ProcessBuilder();
        setCommand(builder, scriptExecutionRequest);
        builder.directory(home.toFile());
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        process.waitFor(timeout, TimeUnit.MINUTES);
    }

    private void setCommand(ProcessBuilder builder, ScriptExecutionRequest scriptExecutionRequest) throws ScriptDoesNotExistException {
        // TODO
        Script script = ScriptExecutionRequestHandlerService.getInstance().getScript(scriptExecutionRequest);
        boolean isWindows = System.getProperty("os.name".toLowerCase()).startsWith("windows");
        if (isWindows) {
            builder.command("cmd.exe", "/c", "iesi-launch.cmd", "-script", script.getName());
        } else {
            builder.command("sh", "-c", "ls");
        }
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
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

}
