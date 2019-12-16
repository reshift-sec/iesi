package io.metadew.iesi.metadata.service.execution.script;

import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.metadata.configuration.script.exception.ScriptDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.script.ScriptFileExecutionRequest;
import io.metadew.iesi.metadata.definition.script.Script;
import io.metadew.iesi.script.operation.JsonInputOperation;
import io.metadew.iesi.script.operation.YamlInputOperation;

import java.io.File;

public class ScriptFileExecutionRequestService implements ScriptExecutionRequestService<ScriptFileExecutionRequest> {

    private static ScriptFileExecutionRequestService INSTANCE;

    public synchronized static ScriptFileExecutionRequestService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptFileExecutionRequestService();
        }
        return INSTANCE;
    }
    private ScriptFileExecutionRequestService() {}

    @Override
    public Script getScript(ScriptFileExecutionRequest scriptFileExecutionRequest) throws ScriptDoesNotExistException {
        File file = new File(scriptFileExecutionRequest.getFileName());
        if (FileTools.getFileExtension(file).equalsIgnoreCase("json")) {
            JsonInputOperation jsonInputOperation = new JsonInputOperation(scriptFileExecutionRequest.getFileName());
            return jsonInputOperation.getScript().orElseThrow(() -> new ScriptDoesNotExistException(""));
        } else if (FileTools.getFileExtension(file).equalsIgnoreCase("yml")) {
            YamlInputOperation yamlInputOperation = new YamlInputOperation(scriptFileExecutionRequest.getFileName());
            return yamlInputOperation.getScript().orElseThrow(() -> new ScriptDoesNotExistException(""));
        } else {
            throw new RuntimeException();
        }

    }

    @Override
    public Class<ScriptFileExecutionRequest> appliesTo() {
        return ScriptFileExecutionRequest.class;
    }
}
