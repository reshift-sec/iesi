package io.metadew.iesi.metadata.service.execution.script;

import io.metadew.iesi.metadata.configuration.script.exception.ScriptDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.metadata.definition.script.Script;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class ScriptExecutionRequestHandlerService {

    private Map<Class<? extends ScriptExecutionRequest>, ScriptExecutionRequestService> scriptExecutionRequestServiceMap;

    private static ScriptExecutionRequestHandlerService INSTANCE;

    public synchronized static ScriptExecutionRequestHandlerService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptExecutionRequestHandlerService();
        }
        return INSTANCE;
    }

    private ScriptExecutionRequestHandlerService() {
        scriptExecutionRequestServiceMap = new HashMap<>();
        ScriptFileExecutionRequestService scriptFileExecutor = ScriptFileExecutionRequestService.getInstance();
        scriptExecutionRequestServiceMap.put(scriptFileExecutor.appliesTo(), scriptFileExecutor);
        ScriptNameExecutionRequestService scriptNameExecutor = ScriptNameExecutionRequestService.getInstance();
        scriptExecutionRequestServiceMap.put(scriptNameExecutor.appliesTo(), scriptNameExecutor);
    }

    @SuppressWarnings("unchecked")
    public Script getScript(ScriptExecutionRequest scriptExecutionRequest) throws ScriptDoesNotExistException {
        ScriptExecutionRequestService scriptExecutionRequestService = scriptExecutionRequestServiceMap.get(scriptExecutionRequest.getClass());

        if (scriptExecutionRequestService == null) {
            throw new RuntimeException(MessageFormat.format("No ScriptExecutionRequestHandler found for request type {0}", scriptExecutionRequest.getClass().getSimpleName()));
        } else {
            return scriptExecutionRequestService.getScript(scriptExecutionRequest);
        }
    }
}
