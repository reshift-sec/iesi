package io.metadew.iesi.runtime.script;

import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ScriptExecutor {

    private final ExecutorService queue;

    protected ScriptExecutor(int threadSize) {
        this.queue = Executors.newFixedThreadPool(threadSize);
    }


    public void start(ScriptExecutionRequest scriptExecutionRequest) {

    }

    public abstract void execute(ScriptExecutionRequest scriptExecutionRequest) throws Exception;

    public ExecutorService getQueue() {
        return queue;
    }
}
