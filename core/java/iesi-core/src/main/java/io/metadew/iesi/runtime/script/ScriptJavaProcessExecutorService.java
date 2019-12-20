package io.metadew.iesi.runtime.script;

import io.metadew.iesi.runtime.script.environment_strategy.ListedEnvironmentSelectionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptJavaProcessExecutorService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static ScriptJavaProcessExecutorService INSTANCE;

    public synchronized static ScriptJavaProcessExecutorService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptJavaProcessExecutorService();
        }
        return INSTANCE;
    }

    public ScriptJavaProcessExecutor fromProperties(Map<String, String> properties) {
        LOGGER.info("Constructing Java Process execution node from " + properties.toString());
        String environments = properties.get("environments");
        String location = properties.get("location");
        String timeout = properties.get("timeout");
        String queueSize = properties.get("queue_size");
        if (location == null) {
            throw new RuntimeException("Java Process execution node needs 'location' to be set");
        }

        if (queueSize == null && environments == null && timeout == null) {
            return new ScriptJavaProcessExecutor(Paths.get(location));
        } else if (queueSize == null && environments == null && timeout != null) {
            return new ScriptJavaProcessExecutor(Paths.get(location), Integer.parseInt(timeout));
        } else if (queueSize == null && environments != null && timeout == null) {
            return new ScriptJavaProcessExecutor(new ListedEnvironmentSelectionStrategy(Arrays.stream(environments.split(",")).collect(Collectors.toList())),
                    Paths.get(location));
        } else if (queueSize != null && environments == null && timeout == null) {
            return new ScriptJavaProcessExecutor(Integer.parseInt(queueSize),
                    Paths.get(location));
        } else if (queueSize == null && environments != null && timeout != null) {
            return new ScriptJavaProcessExecutor(new ListedEnvironmentSelectionStrategy(Arrays.stream(environments.split(",")).collect(Collectors.toList())),
                    Paths.get(location), Integer.parseInt(timeout));
        } else if (queueSize != null && environments != null && timeout == null) {
            return new ScriptJavaProcessExecutor(Integer.parseInt(queueSize),
                    new ListedEnvironmentSelectionStrategy(Arrays.stream(environments.split(",")).collect(Collectors.toList())),
                    Paths.get(location));
        } else if (queueSize != null && environments == null && timeout != null) {
            return new ScriptJavaProcessExecutor(Integer.parseInt(queueSize),
                    Paths.get(location),
                    Integer.parseInt(timeout));
        } else {
            return new ScriptJavaProcessExecutor(Integer.parseInt(queueSize),
                    new ListedEnvironmentSelectionStrategy(Arrays.stream(environments.split(",")).collect(Collectors.toList())),
                    Paths.get(location),
                    Integer.parseInt(timeout));
        }
    }
}
