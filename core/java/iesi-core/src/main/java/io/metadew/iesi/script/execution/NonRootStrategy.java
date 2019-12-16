package io.metadew.iesi.script.execution;

public class NonRootStrategy implements RootingStrategy {

    @Override
    public void prepareExecution(ScriptExecution scriptExecution) {

    }


    @Override
    public void endExecution(ScriptExecution scriptExecution) {
        scriptExecution.getExecutionControl().setActionErrorStop(false);
    }

}
