package io.metadew.iesi.script.execution;

import io.metadew.iesi.metadata.configuration.action.performance.ActionPerformanceConfiguration;
import io.metadew.iesi.metadata.configuration.action.performance.exception.ActionPerformanceAlreadyExistsException;
import io.metadew.iesi.metadata.definition.action.performance.ActionPerformance;
import io.metadew.iesi.metadata.definition.action.performance.key.ActionPerformanceKey;

import java.time.Duration;
import java.time.LocalDateTime;

public class ActionPerformanceLogger {


    public ActionPerformanceLogger() {}

    public void log(ActionExecution actionExecution, String scope, LocalDateTime startTimestamp, LocalDateTime endTimestamp) {
        try {
            ActionPerformanceConfiguration.getInstance().insert(new ActionPerformance(new ActionPerformanceKey(actionExecution.getExecutionControl().getRunId(), actionExecution.getProcessId(), actionExecution.getAction().getId(), scope),
                    actionExecution.getExecutionControl().getEnvName(), startTimestamp, endTimestamp, (double) Duration.between(startTimestamp, endTimestamp).toMillis()));
        } catch (ActionPerformanceAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

}
