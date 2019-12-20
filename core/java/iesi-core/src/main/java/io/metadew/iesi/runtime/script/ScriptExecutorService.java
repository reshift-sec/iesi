package io.metadew.iesi.runtime.script;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ScriptExecutorService {


    private static ScriptExecutorService INSTANCE;

    public synchronized static ScriptExecutorService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptExecutorService();
        }
        return INSTANCE;
    }

    public ScriptExecutor fromProperties(Map<String, String> properties) {
        String type = properties.get("type");
        if (type.equals("memory")) {
            return ScriptMemoryExecutorService.getInstance().fromProperties(properties);
        } else if (type.equals("process")) {
            return ScriptJavaProcessExecutorService.getInstance().fromProperties(properties);
        } else {
            throw new RuntimeException("No execution nodes of type " + type + " can be defined.");
        }

    }
}
