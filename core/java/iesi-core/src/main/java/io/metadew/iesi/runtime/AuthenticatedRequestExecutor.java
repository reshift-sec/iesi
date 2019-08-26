package io.metadew.iesi.runtime;

import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.framework.execution.IESIMessage;
import io.metadew.iesi.guard.configuration.UserAccessConfiguration;
import io.metadew.iesi.guard.definition.UserAccess;
import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.configuration.execution.ExecutionRequestConfiguration;
import io.metadew.iesi.metadata.configuration.script.exception.ScriptDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.AuthenticatedExecutionRequest;
import io.metadew.iesi.metadata.definition.execution.ExecutionRequestStatus;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.runtime.script.ScriptExecutorService;
import io.metadew.iesi.script.ScriptExecutionBuildException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class AuthenticatedRequestExecutor implements RequestExecutor<AuthenticatedExecutionRequest> {

    private static final Logger LOGGER = LogManager.getLogger();
    private final UserAccessConfiguration userAccessConfiguration;
    private final Boolean authenticationEnabled;
    private final ExecutionRequestConfiguration executionRequestConfiguration;

    private static AuthenticatedRequestExecutor INSTANCE;

    public synchronized static AuthenticatedRequestExecutor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticatedRequestExecutor();
        }
        return INSTANCE;
    }

    private AuthenticatedRequestExecutor() {
        this.userAccessConfiguration = new UserAccessConfiguration();
        this.authenticationEnabled = FrameworkSettingConfiguration.getInstance().getSettingPath("guard.authenticate")
                .map(settingPath -> FrameworkControl.getInstance().getProperty(settingPath).equalsIgnoreCase("y"))
                .orElse(false);
        executionRequestConfiguration = new ExecutionRequestConfiguration();
    }

    @Override
    public Class<AuthenticatedExecutionRequest> appliesTo() {
        return AuthenticatedExecutionRequest.class;
    }

    @Override
    public void execute(AuthenticatedExecutionRequest executionRequest) {
        try {
            if (authenticationEnabled) {
                checkUserAccess(executionRequest);
            } else {
                LOGGER.info("authentication.disabled:access automatically granted");
            }
            executionRequest.updateExecutionRequestStatus(ExecutionRequestStatus.ACCEPTED);
            executionRequestConfiguration.update(executionRequest);

            for (ScriptExecutionRequest scriptExecutionRequest : executionRequest.getScriptExecutionRequests()) {
                try {
                    ScriptExecutorService.getInstance().execute(scriptExecutionRequest);
                } catch (ScriptExecutionBuildException | ScriptDoesNotExistException | MetadataAlreadyExistsException e) {
                    // TODO log
                    e.printStackTrace();
                }
            }

            executionRequest.updateExecutionRequestStatus(ExecutionRequestStatus.COMPLETED);
            executionRequestConfiguration.update(executionRequest);

        } catch (SQLException | MetadataDoesNotExistException e) {
            e.printStackTrace();
        }


//
//        RequestResult requestResult = new RequestResult(new RequestResultKey(request.getId()), "-1",
//                request.getType(), FrameworkRuntime.getInstance().getFrameworkRunId(), request.getName(), request.getScope(), request.getContext(), request.getSpace(), request.getUser(),
//                RequestStatus.RUNNING.value(), LocalDateTime.parse(request.getTimestamp()), LocalDateTime.now(), null);
//        requestResultConfiguration.insert(requestResult);
//
//        if (request.getType() != null) {
//            switch (request.getType()) {
//                case "script":
//                    ScriptLaunchOperation.execute(request);
//                    break;
//                default:
//                    throw new RuntimeException("Request type is not supported");
//            }
//        } else {
//            throw new RuntimeException("Empty request submitted for execution");
//        }
//
//        requestResult.setStatus(RequestStatus.SUCCESS.value());
//        requestResult.setEndTimestamp(LocalDateTime.now());
//        requestResultConfiguration.update(requestResult);
//    } catch (Exception e) {
//        StringWriter stackTrace = new StringWriter();
//        e.printStackTrace(new PrintWriter(stackTrace));
//
//        LOGGER.warn("exception=" + e.getMessage());
//        LOGGER.warn("stacktrace=" + stackTrace.toString());
    }

    private void checkUserAccess(AuthenticatedExecutionRequest executionRequest) throws SQLException, MetadataDoesNotExistException {
        UserAccess userAccess = userAccessConfiguration.doUserLogin(executionRequest.getUser(), executionRequest.getPassword());

        if (userAccess.isException()) {
            LOGGER.info(new IESIMessage("guard.user.exception=" + userAccess.getExceptionMessage()));
            LOGGER.info(new IESIMessage("guard.user.denied"));
            executionRequest.updateExecutionRequestStatus(ExecutionRequestStatus.ACCEPTED);
            executionRequestConfiguration.update(executionRequest);
            throw new RuntimeException("guard.user.denied");
        }
    }
}