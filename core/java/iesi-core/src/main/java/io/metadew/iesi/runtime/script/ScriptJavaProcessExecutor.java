package io.metadew.iesi.runtime.script;

import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.script.ScriptExecutionBuildException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ScriptJavaProcessExecutor extends ScriptExecutor {

    private Path home;

    public ScriptJavaProcessExecutor(int threadSize, String path) {
        super(threadSize);
        this.home = Paths.get(path).resolve("bin");
    }

    public void execute(ScriptExecutionRequest scriptExecutionRequest) throws MetadataDoesNotExistException, ScriptExecutionBuildException, MetadataAlreadyExistsException, IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        setCommand(builder, scriptExecutionRequest);
        builder.directory(home.toFile());
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
//
//
//        Script script = ScriptExecutionRequestHandlerService.getInstance().getScript(scriptExecutionRequest);
//
//        ScriptExecution scriptExecution = new ScriptExecutionBuilder(true, false)
//                .script(script)
//                .exitOnCompletion(scriptExecutionRequest.isExit())
//                .parameters(scriptExecutionRequest.getParameters())
//                .impersonations(scriptExecutionRequest.getImpersonations().orElse(new HashMap<>()))
//                .environment(scriptExecutionRequest.getEnvironment())
//                .build();
//
//        io.metadew.iesi.metadata.definition.execution.script.ScriptExecution scriptExecution1 =
//                new io.metadew.iesi.metadata.definition.execution.script.ScriptExecution(new ScriptExecutionKey(),
//                        scriptExecutionRequest.getMetadataKey(), scriptExecution.getExecutionControl().getRunId(),
//                        ScriptRunStatus.RUNNING, LocalDateTime.now(), null);
//        ScriptExecutionConfiguration.getInstance().insert(scriptExecution1);
//
//        scriptExecution.execute();
//        scriptExecution1.updateScriptRunStatus(ScriptResultConfiguration.getInstance().get(new ScriptResultKey(scriptExecution1.getRunId(), -1L))
//                .map(scriptResult -> ScriptRunStatus.valueOf(scriptResult.getStatus()))
//                .orElseThrow(() -> new RuntimeException("Cannot find result of run id: " + scriptExecution1.getRunId())));
//        scriptExecution1.setEndTimestamp(LocalDateTime.now());
//        ScriptExecutionConfiguration.getInstance().update(scriptExecution1);
    }

    private void setCommand(ProcessBuilder builder, ScriptExecutionRequest scriptExecutionRequest) {
        boolean isWindows = System.getProperty("os.name".toLowerCase()).startsWith("windows");
        if (isWindows) {
            builder.command("cmd.exe", "/c", "iesi-launch.cmd", "-script");
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
