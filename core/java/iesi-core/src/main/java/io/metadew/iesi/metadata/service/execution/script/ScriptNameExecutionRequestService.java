package io.metadew.iesi.metadata.service.execution.script;

import io.metadew.iesi.metadata.configuration.script.ScriptConfiguration;
import io.metadew.iesi.metadata.configuration.script.exception.ScriptDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.script.ScriptNameExecutionRequest;
import io.metadew.iesi.metadata.definition.script.Script;

import java.text.MessageFormat;

public class ScriptNameExecutionRequestService implements ScriptExecutionRequestService<ScriptNameExecutionRequest> {

    private final ScriptConfiguration scriptConfiguration;


    private static ScriptNameExecutionRequestService INSTANCE;

    public synchronized static ScriptNameExecutionRequestService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptNameExecutionRequestService();
        }
        return INSTANCE;
    }

    private ScriptNameExecutionRequestService() {
        this.scriptConfiguration = new ScriptConfiguration();
    }

    @Override
    public Script getScript(ScriptNameExecutionRequest scriptNameExecutionRequest) throws ScriptDoesNotExistException {
        return scriptNameExecutionRequest.getScriptVersion()
                .map(scriptVersion -> scriptConfiguration.get(scriptNameExecutionRequest.getScriptName(), scriptVersion))
                .orElse(scriptConfiguration.get(scriptNameExecutionRequest.getScriptName()))
                .orElseThrow(() -> new ScriptDoesNotExistException(MessageFormat.format("Script {0}:{1} does not exist", scriptNameExecutionRequest.getScriptName(), scriptNameExecutionRequest.getScriptVersion().map(Object::toString).orElse("latest"))));
    }

    @Override
    public Class<ScriptNameExecutionRequest> appliesTo() {
        return ScriptNameExecutionRequest.class;
    }
}
