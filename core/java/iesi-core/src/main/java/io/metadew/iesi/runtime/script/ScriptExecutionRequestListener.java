package io.metadew.iesi.runtime.script;

import io.metadew.iesi.framework.configuration.FrameworkConfiguration;
import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;

public class ScriptExecutionRequestListener {

    private final ScriptMemoryExecutor scriptMemoryExecutor;
    private final ScriptJavaProcessExecutor scriptJavaProcessExecutor;

    private static ScriptExecutionRequestListener INSTANCE;

    public synchronized static ScriptExecutionRequestListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptExecutionRequestListener();
        }
        return INSTANCE;
    }

    private ScriptExecutionRequestListener() {
        this.scriptMemoryExecutor = new ScriptMemoryExecutor(2);
        this.scriptJavaProcessExecutor = new ScriptJavaProcessExecutor(2, FrameworkConfiguration.getInstance().getFrameworkHome());
    }

    public void execute(ScriptExecutionRequest scriptExecutionRequest) throws Exception {
        if (FrameworkSettingConfiguration.getInstance().getSettingPath("server.mode")
                .map(settingPath -> FrameworkControl.getInstance().getProperty(settingPath))
                .orElse("off")
                .toLowerCase().equalsIgnoreCase("off")) {
            scriptMemoryExecutor.execute(scriptExecutionRequest);
        } else {
            scriptMemoryExecutor.getQueue().execute(new ScriptExecutionTask(scriptExecutionRequest, scriptMemoryExecutor));
        }
    }
}
