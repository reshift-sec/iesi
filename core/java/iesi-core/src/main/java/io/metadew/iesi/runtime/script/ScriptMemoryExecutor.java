package io.metadew.iesi.runtime.script;

import io.metadew.iesi.framework.configuration.ScriptRunStatus;
import io.metadew.iesi.metadata.configuration.execution.script.ScriptExecutionConfiguration;
import io.metadew.iesi.metadata.configuration.script.result.ScriptResultConfiguration;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.metadata.definition.execution.script.key.ScriptExecutionKey;
import io.metadew.iesi.metadata.definition.script.Script;
import io.metadew.iesi.metadata.definition.script.result.key.ScriptResultKey;
import io.metadew.iesi.metadata.service.execution.script.ScriptExecutionRequestHandlerService;
import io.metadew.iesi.runtime.script.environment_strategy.EnvironmentSelectionStrategy;
import io.metadew.iesi.script.execution.ScriptExecution;
import io.metadew.iesi.script.execution.ScriptExecutionBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;

public class ScriptMemoryExecutor extends ScriptExecutor {

    public ScriptMemoryExecutor(int threadSize) {
        super(threadSize);
    }

    public ScriptMemoryExecutor() {
        super();
    }

    public ScriptMemoryExecutor(int threadSize, EnvironmentSelectionStrategy environmentSelectionStrategy) {
        super(threadSize, environmentSelectionStrategy);
    }
    public ScriptMemoryExecutor(EnvironmentSelectionStrategy environmentSelectionStrategy) {
        super(environmentSelectionStrategy);
    }

    @Override
    public void execute(ScriptExecutionRequest scriptExecutionRequest) throws Exception {
        Script script = ScriptExecutionRequestHandlerService.getInstance().getScript(scriptExecutionRequest);

        ScriptExecution scriptExecution = new ScriptExecutionBuilder(true, false)
                .script(script)
                .exitOnCompletion(scriptExecutionRequest.isExit())
                .parameters(scriptExecutionRequest.getParameters())
                .impersonations(scriptExecutionRequest.getImpersonations().orElse(new HashMap<>()))
                .environment(scriptExecutionRequest.getEnvironment())
                .build();

        io.metadew.iesi.metadata.definition.execution.script.ScriptExecution scriptExecution1 =
                new io.metadew.iesi.metadata.definition.execution.script.ScriptExecution(new ScriptExecutionKey(),
                        scriptExecutionRequest.getMetadataKey(), scriptExecution.getExecutionControl().getRunId(),
                        ScriptRunStatus.RUNNING, LocalDateTime.now(), null);
        ScriptExecutionConfiguration.getInstance().insert(scriptExecution1);

        scriptExecution.execute();
        scriptExecution1.updateScriptRunStatus(ScriptResultConfiguration.getInstance().get(new ScriptResultKey(scriptExecution1.getRunId(), -1L))
                .map(scriptResult -> ScriptRunStatus.valueOf(scriptResult.getStatus()))
                .orElseThrow(() -> new RuntimeException("Cannot find result of run id: " + scriptExecution1.getRunId())));
        scriptExecution1.setEndTimestamp(LocalDateTime.now());
        ScriptExecutionConfiguration.getInstance().update(scriptExecution1);
    }
}
