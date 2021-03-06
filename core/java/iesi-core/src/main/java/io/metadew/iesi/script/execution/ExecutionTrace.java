package io.metadew.iesi.script.execution;

import io.metadew.iesi.metadata.service.action.ActionTraceService;
import io.metadew.iesi.metadata.service.script.ScriptTraceService;
import io.metadew.iesi.script.operation.ActionParameterOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for storing all trace information that is applicable during a script execution
 *
 * @author peter.billen
 */
public class ExecutionTrace {

    private ActionTraceService actionTraceService;
    private ScriptTraceService scriptTraceService;

    // Constructors
    public ExecutionTrace() {
        this.actionTraceService = new ActionTraceService();
        this.scriptTraceService = new ScriptTraceService();
    }

    // Insert
    public void setExecution(ScriptExecution scriptExecution) {
        scriptTraceService.trace(scriptExecution);
    }

    public void setExecution(ActionExecution actionExecution, HashMap<String, ActionParameterOperation> actionParameterOperationMap) {
        actionTraceService.trace(actionExecution, actionParameterOperationMap.entrySet().stream()
                .filter(entry -> entry.getValue().getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())));
    }

}