package io.metadew.iesi.runtime.script;

import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.runtime.script.environment_strategy.DefaultEnvironmentSelectionStrategy;
import io.metadew.iesi.runtime.script.environment_strategy.EnvironmentSelectionStrategy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ScriptExecutor {

    private final ExecutorService queue;
    private final EnvironmentSelectionStrategy environmentSelectionStrategy;

    public ScriptExecutor() {
        this(1);
    }

    public ScriptExecutor(int threadSize) {
        this(threadSize, new DefaultEnvironmentSelectionStrategy());
    }

    public ScriptExecutor(EnvironmentSelectionStrategy environmentSelectionStrategy) {
        this(1, environmentSelectionStrategy);
    }

    public ScriptExecutor(int threadSize, EnvironmentSelectionStrategy environmentSelectionStrategy) {
        this.queue = Executors.newFixedThreadPool(threadSize);
        this.environmentSelectionStrategy = environmentSelectionStrategy;
    }

    public abstract void execute(ScriptExecutionRequest scriptExecutionRequest) throws Exception;

    public ExecutorService getQueue() {
        return queue;
    }

    public EnvironmentSelectionStrategy getEnvironmentSelectionStrategy() {
        return environmentSelectionStrategy;
    }
}
