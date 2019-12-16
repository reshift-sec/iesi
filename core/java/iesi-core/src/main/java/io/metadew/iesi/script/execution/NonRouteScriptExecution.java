package io.metadew.iesi.script.execution;

import io.metadew.iesi.metadata.definition.script.Script;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class NonRouteScriptExecution extends ScriptExecution {

    private static final Logger LOGGER = LogManager.getLogger();

    public NonRouteScriptExecution(Script script, String environment, ExecutionControl executionControl, ExecutionMetrics executionMetrics, Long processId, boolean exitOnCompletion, ScriptExecution parentScriptExecution, Map<String, String> parameters, Map<String, String> impersonations, RootingStrategy rootingStrategy) {
        super(script, environment, executionControl, executionMetrics, processId, exitOnCompletion, parentScriptExecution, parameters, impersonations, rootingStrategy);
    }

    @Override
    public void prepareExecution() {
        LOGGER.info("script.name=" + this.getScript().getName());
        LOGGER.info("script.version=" + this.getScript().getVersion().getNumber());
        LOGGER.info("exec.env=" + this.getExecutionControl().getEnvName());
        this.getExecutionControl().logStart(this);

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            getExecutionControl().getExecutionRuntime().setRuntimeVariable(this, parameter.getKey(), parameter.getValue());
        }

        this.traceDesignMetadata();
    }

    @Override
    protected void endExecution() {
        setResult(getExecutionControl().logEnd(this));
        getRootingStrategy().endExecution(this);
    }

}
