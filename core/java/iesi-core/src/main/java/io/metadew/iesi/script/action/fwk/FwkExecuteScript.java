package io.metadew.iesi.script.action.fwk;

import io.metadew.iesi.common.configuration.ScriptRunStatus;
import io.metadew.iesi.datatypes.DataType;
import io.metadew.iesi.datatypes.text.Text;
import io.metadew.iesi.metadata.configuration.script.ScriptConfiguration;
import io.metadew.iesi.metadata.definition.action.ActionParameter;
import io.metadew.iesi.metadata.definition.script.Script;
import io.metadew.iesi.metadata.definition.script.key.ScriptKey;
import io.metadew.iesi.metadata.tools.IdentifierTools;
import io.metadew.iesi.script.ScriptExecutionBuildException;
import io.metadew.iesi.script.action.ActionTypeExecution;
import io.metadew.iesi.script.execution.ActionExecution;
import io.metadew.iesi.script.execution.ExecutionControl;
import io.metadew.iesi.script.execution.ScriptExecution;
import io.metadew.iesi.script.execution.ScriptExecutionBuilder;
import io.metadew.iesi.script.operation.ActionParameterOperation;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


@Log4j2
public class FwkExecuteScript extends ActionTypeExecution {

    private final Pattern keyValuePattern = Pattern.compile("\\s*(?<parameter>.+)\\s*=\\s*(?<value>.+)\\s*");
    private static final String actionTypeName = "fwk.executeScript";
    private static final String scriptNameKey = "script";
    private static final String scriptVersionKey = "version";
    private static final String environmentKey = "environment";
    private static final String parameterListKey = "paramList";
    private static final String parameterFileKey = "paramFile";


    private String scriptName;
    private Long scriptVersion;
    private String environmentName;
    private String parameterList;
    private String parameterFilename;

    public FwkExecuteScript(ExecutionControl executionControl, ScriptExecution scriptExecution, ActionExecution actionExecution) {
        super(executionControl, scriptExecution, actionExecution);
    }

    public void prepare() {
        // Reset Parameters
        ActionParameterOperation scriptNameActionParameterOperation = new ActionParameterOperation(getExecutionControl(), getActionExecution(), actionTypeName, scriptNameKey);
        ActionParameterOperation scriptVersionActionParameterOperation = new ActionParameterOperation(getExecutionControl(), getActionExecution(), actionTypeName, scriptVersionKey);
        ActionParameterOperation environmentNameActionParameterOperation = new ActionParameterOperation(getExecutionControl(), getActionExecution(), actionTypeName, environmentKey);
        ActionParameterOperation parameterListActionParameterOperation = new ActionParameterOperation(getExecutionControl(), getActionExecution(), actionTypeName, parameterListKey);
        ActionParameterOperation parameterFileActionParameterOperation = new ActionParameterOperation(getExecutionControl(), getActionExecution(), actionTypeName, parameterFileKey);

        // Get Parameters
        for (ActionParameter actionParameter : getActionExecution().getAction().getParameters()) {
            if (actionParameter.getMetadataKey().getParameterName().equalsIgnoreCase(scriptNameKey)) {
                scriptNameActionParameterOperation.setInputValue(actionParameter.getValue(), getExecutionControl().getExecutionRuntime());
            } else if (actionParameter.getMetadataKey().getParameterName().equalsIgnoreCase(scriptVersionKey)) {
                scriptVersionActionParameterOperation.setInputValue(actionParameter.getValue(), getExecutionControl().getExecutionRuntime());
            } else if (actionParameter.getMetadataKey().getParameterName().equalsIgnoreCase(environmentKey)) {
                environmentNameActionParameterOperation.setInputValue(actionParameter.getValue(), getExecutionControl().getExecutionRuntime());
            } else if (actionParameter.getMetadataKey().getParameterName().equalsIgnoreCase(parameterListKey)) {
                parameterListActionParameterOperation.setInputValue(actionParameter.getValue(), getExecutionControl().getExecutionRuntime());
            } else if (actionParameter.getMetadataKey().getParameterName().equalsIgnoreCase(parameterFileKey)) {
                parameterFileActionParameterOperation.setInputValue(actionParameter.getValue(), getExecutionControl().getExecutionRuntime());
            }
        }

        // Create parameter list
        getActionParameterOperationMap().put(scriptNameKey, scriptNameActionParameterOperation);
        getActionParameterOperationMap().put(scriptVersionKey, scriptVersionActionParameterOperation);
        getActionParameterOperationMap().put(environmentKey, environmentNameActionParameterOperation);
        getActionParameterOperationMap().put(parameterListKey, parameterListActionParameterOperation);
        getActionParameterOperationMap().put(parameterFileKey, parameterFileActionParameterOperation);

        scriptName = convertScriptName(scriptNameActionParameterOperation.getValue());
        scriptVersion = convertScriptVersion(scriptVersionActionParameterOperation.getValue());
        environmentName = convertEnvironmentName(environmentNameActionParameterOperation.getValue());
        parameterList = convertParameterList(parameterListActionParameterOperation.getValue());
        parameterFilename = convertParameterFileName(parameterFileActionParameterOperation.getValue());

    }

    protected boolean executeAction() throws ScriptExecutionBuildException, InterruptedException {
        if (getScriptExecution().getScript().getName().equals(scriptName)) {
            throw new RuntimeException(MessageFormat.format("Not allowed to run the script recursively. Attempting to run {0} in {1}", scriptName, getScriptExecution().getScript().getName()));
        }

        Script script;
        if (scriptVersion == null) {
            script = ScriptConfiguration.getInstance().getLatestVersion(scriptName)
                    .orElseThrow(() -> new RuntimeException(MessageFormat.format("No implementation for script {0} found", scriptName)));
        } else {
            log.info("no version selected, choosing latest version of script " + scriptName);
            script = ScriptConfiguration.getInstance()
                    .get(new ScriptKey(IdentifierTools.getScriptIdentifier(scriptName), scriptVersion))
                    .orElseThrow(() -> new RuntimeException(MessageFormat.format("No implementation for script {0}-{1} found", scriptName, scriptVersion)));
        }

        Map<String, String> parameters = new HashMap<>();
        if (parameterFilename != null) {
            parameters.putAll(parseParameterFiles(parameterFilename));
        }
        if (parameterList != null) {
            parameters.putAll(parseParameterRepresentation(parameterList));
        }

        ScriptExecution subScriptScriptExecution = new ScriptExecutionBuilder(false, false)
                .script(script)
                .executionControl(getExecutionControl())
                .processId(getExecutionControl().getLastProcessId())
                .parentScriptExecution(getScriptExecution())
                .exitOnCompletion(false)
                .parameters(parameters)
                .environment(getExecutionControl().getEnvName())
                .build();

        subScriptScriptExecution.execute();

        if (subScriptScriptExecution.getResult().equalsIgnoreCase(ScriptRunStatus.SUCCESS.value())) {
            getActionExecution().getActionControl().increaseSuccessCount();
        } else if (subScriptScriptExecution.getResult().equalsIgnoreCase(ScriptRunStatus.WARNING.value())) {
            getActionExecution().getActionControl().increaseWarningCount();
        } else if (subScriptScriptExecution.getResult()
                .equalsIgnoreCase(ScriptRunStatus.ERROR.value())) {
            getActionExecution().getActionControl().increaseErrorCount();
        } else {
            getActionExecution().getActionControl().increaseErrorCount();
        }

        return true;
    }

    private String convertParameterList(DataType parameterList) {
        if (parameterList == null) {
            return null;
        }
        if (parameterList instanceof Text) {
            return parameterList.toString();
        } else {
            log.warn(MessageFormat.format(actionTypeName + " does not accept {0} as type for parameterList",
                    parameterList.getClass()));
            return null;
        }
    }

    private Long convertScriptVersion(DataType scriptVersion) {
        if (scriptVersion == null) {
            return null;
        }
        if (scriptVersion instanceof Text) {
            return Long.parseLong(scriptVersion.toString());
        } else {
            log.warn(MessageFormat.format("fwk.executeScript does not accept {0} as type for script name",
                    scriptVersion.getClass()));
            return null;
        }
    }

    private String convertParameterFileName(DataType parameterFileName) {
        if (parameterFileName == null) {
            return null;
        }
        if (parameterFileName instanceof Text) {
            return parameterFileName.toString();
        } else {
            log.warn(MessageFormat.format("fwk.executeScript does not accept {0} as type for parameter file name",
                    parameterFileName.getClass()));
            return null;
        }
    }

    private String convertEnvironmentName(DataType environmentName) {
        if (environmentName == null) {
            return null;
        }
        // TODO: if null get current Environment, here or in execute(...)
        if (environmentName instanceof Text) {
            return environmentName.toString();
        } else {
            log.warn(MessageFormat.format("fwk.executeScript does not accept {0} as type for environment name",
                    environmentName.getClass()));
            return environmentName.toString();
        }
    }

    private String convertScriptName(DataType scriptName) {
        if (scriptName instanceof Text) {
            return scriptName.toString();
        } else {
            log.warn(MessageFormat.format("fwk.executeScript does not accept {0} as type for script name",
                    scriptName.getClass()));
            return scriptName.toString();
        }
    }

    public Map<String, String> parseParameterFiles(String files) {
        Map<String, String> parameters = new HashMap<>();
        String[] parts = files.split(",");
        for (int i = 0; i < parts.length; i++) {
            String innerpart = parts[i];
            parameters.putAll(parseParameterFile(innerpart));
        }
        return parameters;
    }

    public Map<String, String> parseParameterFile(String file) {
        Map<String, String> parameters = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                int delim = line.indexOf("=");
                if (delim > 0) {
                    parameters.put(line.substring(0, delim), line.substring(delim + 1));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parameters;
    }

    public Map<String, String> parseParameterRepresentation(String parametersRepresentation) {
        Map<String, String> parameters = new HashMap<>();
        for (String parameterCombination : parametersRepresentation.split(",")) {
            String[] parameter = parameterCombination.split("=");
            if (parameter.length == 2) {
                parameters.put(parameter[0], parameter[1]);
            }
        }
        return parameters;
    }

}