//package io.metadew.iesi.runtime.script;
//
//import io.metadew.iesi.metadata.configuration.execution.script.ScriptExecutionRequestConfiguration;
//import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
//import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequestStatus;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//public class ScriptExecutorService {
//
//    private static final Logger LOGGER = LogManager.getLogger();
//    private static ScriptExecutorService INSTANCE;
//
//    public synchronized static ScriptExecutorService getInstance() {
//        if (INSTANCE == null) {
//            INSTANCE = new ScriptExecutorService();
//        }
//        return INSTANCE;
//    }
//
//    private ScriptExecutorService() {
//    }
//
//    public void execute(ScriptExecutionRequest scriptExecutionRequest) throws Exception {
//
//        scriptExecutionRequest.updateScriptExecutionRequestStatus(ScriptExecutionRequestStatus.SUBMITTED);
//        ScriptExecutionRequestConfiguration.getInstance().update(scriptExecutionRequest);
//        scriptExecutionRequest.updateScriptExecutionRequestStatus(ScriptExecutionRequestStatus.ACCEPTED);
//        ScriptExecutionRequestConfiguration.getInstance().update(scriptExecutionRequest);
//
//        // ScriptMemoryExecutor.getInstance().execute(scriptExecutionRequest);
//
//        scriptExecutionRequest.updateScriptExecutionRequestStatus(ScriptExecutionRequestStatus.COMPLETED);
//        ScriptExecutionRequestConfiguration.getInstance().update(scriptExecutionRequest);
//    }
//}
