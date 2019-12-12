package io.metadew.iesi.script.execution;

import io.metadew.iesi.common.text.TextTools;
import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.framework.configuration.ScriptRunStatus;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.metadata.configuration.action.result.ActionResultConfiguration;
import io.metadew.iesi.metadata.configuration.action.result.ActionResultOutputConfiguration;
import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.configuration.script.result.ScriptResultConfiguration;
import io.metadew.iesi.metadata.configuration.script.result.ScriptResultOutputConfiguration;
import io.metadew.iesi.metadata.configuration.script.result.exception.ScriptResultDoesNotExistException;
import io.metadew.iesi.metadata.definition.action.result.ActionResult;
import io.metadew.iesi.metadata.definition.action.result.ActionResultOutput;
import io.metadew.iesi.metadata.definition.action.result.key.ActionResultKey;
import io.metadew.iesi.metadata.definition.action.result.key.ActionResultOutputKey;
import io.metadew.iesi.metadata.definition.script.result.ScriptResult;
import io.metadew.iesi.metadata.definition.script.result.ScriptResultOutput;
import io.metadew.iesi.metadata.definition.script.result.key.ScriptResultKey;
import io.metadew.iesi.metadata.definition.script.result.key.ScriptResultOutputKey;
import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.metadata.service.script.ScriptDesignTraceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;

public class ExecutionControl {

    private ExecutionRuntime executionRuntime;
    private String runId;
    private String envName;
    private boolean actionErrorStop = false;
    private boolean scriptExit = false;
    private Long lastProcessId;

    private static final Logger LOGGER = LogManager.getLogger();

    public ExecutionControl(String runId) throws NoSuchMethodException, IllegalAccessException, InstantiationException, SQLException, InvocationTargetException, ClassNotFoundException {
        this.runId = runId;
        initializeExecutionRuntime(runId);
        this.lastProcessId = -1L;
    }

    @SuppressWarnings("unchecked")
    private void initializeExecutionRuntime(String runId) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, SQLException {
        if (FrameworkSettingConfiguration.getInstance().getSettingPath("script.execution.runtime").isPresent() &&
                !FrameworkControl.getInstance().getProperty(FrameworkSettingConfiguration.getInstance().getSettingPath("script.execution.runtime").get()).isEmpty()) {
            Class classRef = Class.forName(FrameworkControl.getInstance().getProperty(FrameworkSettingConfiguration.getInstance().getSettingPath("script.execution.runtime").get()));
            Class[] initParams = {ExecutionControl.class, String.class};
            Constructor constructor = classRef.getConstructor(initParams);
            this.executionRuntime = (ExecutionRuntime) constructor.newInstance(this, runId);
        } else {
            this.executionRuntime = new ExecutionRuntime(this, runId);
        }
    }

    public void setEnvironment(ActionExecution actionExecution, String environmentName) {
        this.envName = environmentName;

        // Set environment variables
        executionRuntime.setRuntimeVariablesFromList(actionExecution, MetadataControl.getInstance()
                .getConnectivityMetadataRepository()
                .executeQuery("select env_par_nm, env_par_val from "
                        + MetadataControl.getInstance().getConnectivityMetadataRepository().getTableNameByLabel("EnvironmentParameters")
                        + " where env_nm = " + SQLTools.GetStringForSQL(this.envName) + " order by env_par_nm asc, env_par_val asc", "reader"));
    }

    public void terminate() {
        this.executionRuntime.terminate();
    }

    // Log start
    public void logStart(ScriptExecution scriptExecution) {
        try {
            Long parentProcessId = scriptExecution.getParentScriptExecution().map(ScriptExecution::getProcessId).orElse(-1L);
            ScriptResult scriptResult = new ScriptResult(new ScriptResultKey(runId, scriptExecution.getProcessId()),
                    parentProcessId,
                    scriptExecution.getScript().getId(),
                    scriptExecution.getScript().getName(),
                    scriptExecution.getScript().getVersion().getNumber(),
                    envName,
                    "ACTIVE",
                    LocalDateTime.now(),
                    null
            );
            ScriptResultConfiguration.getInstance().insert(scriptResult);
            ScriptDesignTraceService.getInstance().trace(scriptExecution);
        } catch (MetadataAlreadyExistsException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace=" + stackTrace.toString());
        }
    }

    public void logStart(ActionExecution actionExecution) {
        try {
            ActionResult actionResult = new ActionResult(
                    runId,
                    actionExecution.getProcessId(),
                    actionExecution.getAction().getId(),
                    actionExecution.getScriptExecution().getProcessId(),
                    actionExecution.getAction().getName(),
                    envName,
                    "ACTIVE",
                    LocalDateTime.now(),
                    null
            );
            ActionResultConfiguration.getInstance().insert(actionResult);
        } catch (MetadataAlreadyExistsException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace=" + stackTrace.toString());
        }
    }

    public void logSkip(ActionExecution actionExecution) {
        try {
            ActionResult actionResult = new ActionResult(
                    runId,
                    actionExecution.getProcessId(),
                    actionExecution.getAction().getId(),
                    actionExecution.getScriptExecution().getProcessId(),
                    actionExecution.getAction().getName(),
                    envName,
                    "SKIPPED",
                    null,
                    null
            );
            ActionResultConfiguration.getInstance().insert(actionResult);

            LOGGER.info("action.status=" + ScriptRunStatus.SKIPPED.value());
        } catch (MetadataAlreadyExistsException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace=" + stackTrace.toString());
        }

    }

    public Long getNextProcessId() {
        lastProcessId = lastProcessId + 1;
        return lastProcessId;
    }

    public Long getProcessId() {
        return lastProcessId;
    }

    public String logEnd(ScriptExecution scriptExecution) {
        try {
            ScriptResult scriptResult = ScriptResultConfiguration.getInstance().get(new ScriptResultKey(runId, scriptExecution.getProcessId()))
                    .orElseThrow(() -> new ScriptResultDoesNotExistException(MessageFormat.format("ScriptResult {0} does not exist, cannot log ending of execution", new ScriptResultKey(runId, scriptExecution.getProcessId()).toString())));

            String status = getStatus(scriptExecution);
            scriptResult.setStatus(status);
            scriptResult.setEndTimestamp(LocalDateTime.now());
            ScriptResultConfiguration.getInstance().update(scriptResult);

            return status;

        } catch (MetadataDoesNotExistException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace=" + stackTrace.toString());

            return ScriptRunStatus.UNKNOWN.value();
        }
    }

    public void logEnd(ActionExecution actionExecution, ScriptExecution scriptExecution) {
        try {
            ActionResult actionResult = ActionResultConfiguration.getInstance().get(new ActionResultKey(runId, actionExecution.getProcessId(), actionExecution.getAction().getId()))
                    .orElseThrow(() -> new ScriptResultDoesNotExistException(MessageFormat.format("ActionResult {0} does not exist, cannot log ending of execution", new ScriptResultKey(runId, scriptExecution.getProcessId()).toString())));

            String status = getStatus(actionExecution, scriptExecution);
            actionResult.setStatus(status);
            actionResult.setEndTimestamp(LocalDateTime.now());
            ActionResultConfiguration.getInstance().update(actionResult);

        } catch (MetadataDoesNotExistException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace=" + stackTrace.toString());
        }
    }

    private String getStatus(ActionExecution actionExecution, ScriptExecution scriptExecution) {
        String status;

        if (actionExecution.getActionControl().getExecutionMetrics().getSkipCount() == 0) {

            if (actionExecution.getActionControl().getExecutionMetrics().getErrorCount() > 0) {
                status = ScriptRunStatus.ERROR.value();
                scriptExecution.getExecutionMetrics().increaseErrorCount(1);
            } else if (actionExecution.getActionControl().getExecutionMetrics().getWarningCount() > 0) {
                status = ScriptRunStatus.WARNING.value();
                scriptExecution.getExecutionMetrics().increaseWarningCount(1);
            } else {
                status = ScriptRunStatus.SUCCESS.value();
                scriptExecution.getExecutionMetrics().increaseSuccessCount(1);
            }
        } else {
            status = ScriptRunStatus.SKIPPED.value();
            scriptExecution.getExecutionMetrics().increaseSkipCount(1);
        }

        LOGGER.info( "action.status=" + status);
        return status;

    }

    private String getStatus(ScriptExecution scriptExecution) {
        String status;

        if (actionErrorStop) {
            status = ScriptRunStatus.STOPPED.value();
        } else if (scriptExit) {
            status = ScriptRunStatus.STOPPED.value();
        } else if (scriptExecution.getExecutionMetrics().getSuccessCount() == 0
                && scriptExecution.getExecutionMetrics().getWarningCount() == 0
                && scriptExecution.getExecutionMetrics().getErrorCount() > 0) {
            status = ScriptRunStatus.ERROR.value();
        } else if (scriptExecution.getExecutionMetrics().getSuccessCount() > 0
                && scriptExecution.getExecutionMetrics().getWarningCount() == 0
                && scriptExecution.getExecutionMetrics().getErrorCount() == 0) {
            status = ScriptRunStatus.SUCCESS.value();
        } else {
            status = ScriptRunStatus.WARNING.value();
        }

        LOGGER.info("script.status=" + status);

        String output = scriptExecution.getExecutionControl().getExecutionRuntime().resolveVariables("#output#");
        if (output != null && !output.isEmpty()) {
            //logMessage(scriptExecution, "script.output=" + output, Level.INFO);
            logExecutionOutput(scriptExecution, "output", output);
        }
        return status;
    }

    public void logExecutionOutput(ScriptExecution scriptExecution, String outputName, String outputValue) {
        // Redact any encrypted values
        outputValue = FrameworkCrypto.getInstance().redact(outputValue);
        outputValue = TextTools.shortenTextForDatabase(outputValue, 2000);
        try {
            LOGGER.info("script.output=" + outputName + ":" + outputValue);
            ScriptResultOutput scriptResultOutput = new ScriptResultOutput(new ScriptResultOutputKey(runId, scriptExecution.getProcessId(), outputName), scriptExecution.getScript().getId(), outputValue);
            ScriptResultOutputConfiguration.getInstance().insert(scriptResultOutput);
        } catch (MetadataAlreadyExistsException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace=" + stackTrace.toString());
        }
    }

    public void logExecutionOutput(ActionExecution actionExecution, String outputName, String outputValue) {
        try {
            // Redact any encrypted values
            outputValue = FrameworkCrypto.getInstance().redact(outputValue);
            // TODO: why shorten?
            outputValue = TextTools.shortenTextForDatabase(outputValue, 2000);

            ActionResultOutput actionResultOutput = new ActionResultOutput(
                    new ActionResultOutputKey(runId, actionExecution.getProcessId(), actionExecution.getAction().getId(), outputName),
                    outputValue);
            ActionResultOutputConfiguration.getInstance().insert(actionResultOutput);

        } catch (MetadataAlreadyExistsException e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace=" + stackTrace.toString());
        }
    }

    public void endExecution() {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("script.launcher.end");
        System.exit(0);
    }

    public String getRunId() {
        return runId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public void setActionErrorStop(boolean actionErrorStop) {
        this.actionErrorStop = actionErrorStop;
    }

    public ExecutionRuntime getExecutionRuntime() {
        return executionRuntime;
    }

    public Long getLastProcessId() {
        return lastProcessId;
    }

    public void setScriptExit(boolean scriptExit) {
        this.scriptExit = scriptExit;
    }
}