package io.metadew.iesi.runtime.script;

import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScriptExecutionTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ScriptExecutor scriptExecutor;
    private ScriptExecutionRequest scriptExecutionRequest;

    public ScriptExecutionTask(ScriptExecutionRequest scriptExecutionRequest, ScriptExecutor scriptExecutor) {
        this.scriptExecutionRequest = scriptExecutionRequest;
        this.scriptExecutor = scriptExecutor;
    }

    @Override
    public void run() {
        try {
            scriptExecutor.execute(scriptExecutionRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
