package io.metadew.iesi.script.execution;

public interface RootingStrategy {

    public void prepareExecution(ScriptExecution scriptExecution);

    public void endExecution(ScriptExecution scriptExecution);

}
