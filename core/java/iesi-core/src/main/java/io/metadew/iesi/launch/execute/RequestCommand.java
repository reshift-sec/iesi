package io.metadew.iesi.launch.execute;

import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.launch.Command;
import io.metadew.iesi.metadata.configuration.execution.ExecutionRequestConfiguration;
import io.metadew.iesi.metadata.definition.execution.ExecutionRequest;
import io.metadew.iesi.metadata.definition.execution.ExecutionRequestBuilder;
import io.metadew.iesi.metadata.definition.execution.ExecutionRequestStatus;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequestBuilder;
import io.metadew.iesi.runtime.ExecutorService;
import picocli.CommandLine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "request", mixinStandardHelpOptions = true, version = "0.2.0")
public class RequestCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Command command;

    @CommandLine.Option(names = "-user", description = "user")
    private String user;
    @CommandLine.Option(names = "-password", interactive = true, required = false)
    private char[] password;

    @CommandLine.Option(names = "-name", required = true, description = "the script name to execute")
    private String name;
    @CommandLine.Option(names = "-env", description = "the environment name where the execution needs to take place", required = true)
    private String environment;
    @CommandLine.Option(names = "-version", description = "script version to execute. If omitted, the latest version will be executed")
    private String version;
    @CommandLine.Option(names = "-parameters", description = "the parameters to be passed to the script")
    private String parameters;


    public static void main(String[] args) {
        System.exit(new CommandLine(new RequestCommand()).execute(args));
    }

    @Override
    public Integer call() {
        try {
            System.out.println("request.launcher.start");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            Command.initFrameworkInstance(command.ini);

            ExecutionRequestBuilder executionRequestBuilder = new ExecutionRequestBuilder();
            ScriptExecutionRequestBuilder scriptExecutionRequestBuilder = new ScriptExecutionRequestBuilder();

            executionRequestBuilder.name("scriptLauncher");
            executionRequestBuilder.scope("execution_request");
            executionRequestBuilder.context("on_demand");

            scriptExecutionRequestBuilder.mode("script");
            scriptExecutionRequestBuilder.scriptName(name);

            if (version != null) {
                scriptExecutionRequestBuilder.scriptVersion(Long.parseLong(version));
            }
            
            scriptExecutionRequestBuilder.environment(environment);
            
            if (parameters != null) {
                scriptExecutionRequestBuilder.parameters(parseParameterRepresentation(parameters));
            }
            if (user != null) {
                executionRequestBuilder.user(user);
            }
            if (password != null) {
                executionRequestBuilder.password(new String(password));
            }

            ExecutionRequest executionRequest = executionRequestBuilder.build();
            scriptExecutionRequestBuilder.executionRequestKey(executionRequest.getMetadataKey());
            executionRequest.setScriptExecutionRequests(Collections.singletonList(scriptExecutionRequestBuilder.build()));

            ExecutionRequestConfiguration.getInstance().insert(executionRequest);

            String serverMode = FrameworkSettingConfiguration.getInstance().getSettingPath("server.mode")
                    .map(settingPath -> FrameworkControl.getInstance().getProperty(settingPath))
                    .orElse("off")
                    .toLowerCase();
            if (serverMode.equalsIgnoreCase("off")) {
                executionRequest.updateExecutionRequestStatus(ExecutionRequestStatus.SUBMITTED);
                ExecutionRequestConfiguration.getInstance().update(executionRequest);
                ExecutorService.getInstance().execute(executionRequest);
            } else if (serverMode.equalsIgnoreCase("standalone")) {
                System.out.println("RequestID=" + executionRequest.getMetadataKey().getId());
            } else {
                throw new RuntimeException("unknown setting for " + FrameworkSettingConfiguration.getInstance().getSettingPath("server.mode").get());
            }

            FrameworkInstance.getInstance().shutdown();
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("request.launcher.end");

            return 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }

    private static Map<String, String> parseParameterRepresentation(String parametersRepresentation) {
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
