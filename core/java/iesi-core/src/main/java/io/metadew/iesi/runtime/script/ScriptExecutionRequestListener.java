package io.metadew.iesi.runtime.script;

import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.metadata.configuration.environment.EnvironmentConfiguration;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScriptExecutionRequestListener {

    private static ScriptExecutionRequestListener INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern NODE_NUMBER_PATTERN = Pattern.compile("iesi.execution_node\\.(?<nodenumber>\\d+)\\..*");
    private Map<String, RoundRobin<ScriptExecutor>> scriptExecutorMap;

    public synchronized static ScriptExecutionRequestListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptExecutionRequestListener();
        }
        return INSTANCE;
    }

    private ScriptExecutionRequestListener() {
        List<ScriptExecutor> scriptExecutors = new ArrayList<>();
        List<Integer> nodeNumbers = FrameworkControl.getInstance().getProperties().keySet().stream()
                .filter(key -> NODE_NUMBER_PATTERN.matcher(key.toString()).find())
                .map(key -> {
                    Matcher matcher = NODE_NUMBER_PATTERN.matcher(key.toString());
                    matcher.find();
                    return Integer.parseInt(matcher.group("nodenumber"));
                })
                .distinct()
                .collect(Collectors.toList());
        LOGGER.info("found " + nodeNumbers.size() + " execution nodes");
        for (Integer nodeNumber : nodeNumbers) {
            Pattern NODE_NUMBER_PROPERTIES_PATTERN = Pattern.compile("execution_node\\." + nodeNumber.toString() + "\\.(?<properties>.*)");
            Map<String, String> nodeProperties = FrameworkControl.getInstance().getProperties().entrySet().stream()
                    .filter(entry -> NODE_NUMBER_PROPERTIES_PATTERN.matcher(entry.getKey().toString()).find())
                    .map(entry -> {
                        Matcher matcher = NODE_NUMBER_PROPERTIES_PATTERN.matcher(entry.getKey().toString());
                        matcher.find();
                        return new AbstractMap.SimpleEntry<>(matcher.group("properties"), entry.getValue().toString());
                    })
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
            scriptExecutors.add(ScriptExecutorService.getInstance().fromProperties(nodeProperties));
        }

        this.scriptExecutorMap = new EnvironmentConfiguration().getAllEnvironments()
                .stream()
                .map(environment -> new AbstractMap.SimpleEntry<>(environment.getName(),
                        new RoundRobin<>(scriptExecutors.stream()
                                .filter(scriptExecutor -> scriptExecutor.getEnvironmentSelectionStrategy().accepts(environment.getName()))
                                .collect(Collectors.toList()))))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public void execute(ScriptExecutionRequest scriptExecutionRequest) throws Exception {
        RoundRobin<ScriptExecutor> scriptExecutorRoundRobin = scriptExecutorMap.get(scriptExecutionRequest.getEnvironment());
        if (scriptExecutorRoundRobin == null) {
            throw new RuntimeException("No execution node found for environment " + scriptExecutionRequest.getEnvironment());
        }
        scriptExecutorRoundRobin.iterator().next().execute(scriptExecutionRequest);
    }
}
