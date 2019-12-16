package io.metadew.iesi.runtime.script;

import io.metadew.iesi.framework.configuration.FrameworkConfiguration;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;

public class ScriptExecutionRequestListener {

    private final ScriptMemoryExecutor scriptMemoryExecutor;
    private final ScriptJavaProcessExecutor scriptJavaProcessExecutor;

    public ScriptExecutionRequestListener() {
        this.scriptMemoryExecutor = new ScriptMemoryExecutor(2);
        this.scriptJavaProcessExecutor = new ScriptJavaProcessExecutor(2, FrameworkConfiguration.getInstance().getFrameworkHome());
    }

    public void execute(ScriptExecutionRequest scriptExecutionRequest) {
        // TODO: Queue
        scriptMemoryExecutor.getQueue().execute(new ScriptExecutionTask(scriptExecutionRequest, scriptMemoryExecutor));
    }
}
