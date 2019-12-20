package io.metadew.iesi.launch.execute;

import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.launch.Command;
import io.metadew.iesi.metadata.configuration.script.ScriptConfiguration;
import io.metadew.iesi.script.execution.ScriptExecutionBuilder;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "execute", mixinStandardHelpOptions = true, version = "0.2.0")
public class ExecuteCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Command command;

    @CommandLine.Option(names = "-env", description = "the environment name where the execution needs to take place", required = true)
    private String environment;
    @CommandLine.Option(names = "-name", required = true, description = "the script name to execute")
    private String name;
    @CommandLine.Option(names = "-version", description = "script version to execute. If omitted, the latest version will be executed")
    private String version;
    @CommandLine.Option(names = "-parameters", description = "the parameters to be passed to the script")
    private String parameters;


    public static void main(String[] args) {
        System.exit(new CommandLine(new ExecuteCommand()).execute(args));
    }

    @Override
    public Integer call() {
        try {
            System.out.println("script.launcher.start");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            Command.initFrameworkInstance(command.ini);

            ScriptExecutionBuilder scriptExecutionBuilder = new ScriptExecutionBuilder(true, false);

            if (version != null) {
                scriptExecutionBuilder.script(new ScriptConfiguration().get(name, Long.parseLong(version))
                        .orElseThrow(() -> new RuntimeException("Could not find script " + name + "-" + version)));
            } else {
                scriptExecutionBuilder.script(new ScriptConfiguration().get(name)
                        .orElseThrow(() -> new RuntimeException("Could not find script " + name)));
            }

            scriptExecutionBuilder.environment(environment);
            // Get variable configurations
            if (parameters != null) {
                scriptExecutionBuilder.parameters(parseParameterRepresentation(parameters));
            }

            scriptExecutionBuilder.build().execute();
            FrameworkInstance.getInstance().shutdown();

            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("script.launcher.end");
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
