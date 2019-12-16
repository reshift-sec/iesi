package io.metadew.iesi.metadata.service.execution.script;

import io.metadew.iesi.metadata.configuration.script.exception.ScriptDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.metadata.definition.script.Script;

public interface ScriptExecutionRequestService<T extends ScriptExecutionRequest> {

    public Script getScript(T scriptExecutionRequest) throws ScriptDoesNotExistException;
    public Class<T> appliesTo();

}
