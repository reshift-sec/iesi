package io.metadew.iesi.runtime;

import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.configuration.script.exception.ScriptDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.NonAuthenticatedExecutionRequest;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.runtime.script.ScriptExecutorService;
import io.metadew.iesi.script.ScriptExecutionBuildException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class NonAuthenticatedRequestExecutor implements RequestExecutor<NonAuthenticatedExecutionRequest> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static NonAuthenticatedRequestExecutor INSTANCE;

    public synchronized static NonAuthenticatedRequestExecutor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NonAuthenticatedRequestExecutor();
        }
        return INSTANCE;
    }

    private NonAuthenticatedRequestExecutor() {}

    @Override
    public Class<NonAuthenticatedExecutionRequest> appliesTo() {
        return NonAuthenticatedExecutionRequest.class;
    }

    @Override
    public void execute(NonAuthenticatedExecutionRequest executionRequest) {
        for (ScriptExecutionRequest scriptExecutionRequest : executionRequest.getScriptExecutionRequests()) {
            try {
                ScriptExecutorService.getInstance().execute(scriptExecutionRequest);
            } catch (ScriptExecutionBuildException | MetadataAlreadyExistsException | SQLException | MetadataDoesNotExistException e) {
                e.printStackTrace();
            }
        }
    }
}
