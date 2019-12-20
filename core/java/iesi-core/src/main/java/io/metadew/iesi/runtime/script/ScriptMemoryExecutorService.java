package io.metadew.iesi.runtime.script;

import io.metadew.iesi.runtime.script.environment_strategy.ListedEnvironmentSelectionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class ScriptMemoryExecutorService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static ScriptMemoryExecutorService INSTANCE;

    public synchronized static ScriptMemoryExecutorService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptMemoryExecutorService();
        }
        return INSTANCE;
    }

    public ScriptMemoryExecutor fromProperties(Map<String, String> properties) {
        LOGGER.info("Constructing in-memory execution node from " + properties.toString());
        String queueSize = properties.get("queue_size");
        String environments = properties.get("environments");

        if (queueSize == null && environments == null) {
            return new ScriptMemoryExecutor();
        } else if (queueSize != null && environments == null) {
            return new ScriptMemoryExecutor(Integer.parseInt(queueSize));
        } else if (queueSize == null) {
            return new ScriptMemoryExecutor(new ListedEnvironmentSelectionStrategy(Arrays.stream(environments.split(",")).collect(Collectors.toList())));
        } else {
            return new ScriptMemoryExecutor(Integer.parseInt(queueSize), new ListedEnvironmentSelectionStrategy(Arrays.stream(environments.split(",")).collect(Collectors.toList())));
        }
    }
}
