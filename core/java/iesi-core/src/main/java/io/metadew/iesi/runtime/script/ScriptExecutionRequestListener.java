package io.metadew.iesi.runtime.script;

import io.metadew.iesi.framework.configuration.FrameworkConfiguration;
import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;

import java.nio.file.Paths;

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
        this.scriptJavaProcessExecutor = new ScriptJavaProcessExecutor(2, Paths.get("C:\\Users\\robbe.berrevoets\\IESISandbox\\v0.2.0\\b2"));
    }

    public void execute(ScriptExecutionRequest scriptExecutionRequest) throws Exception {
        if (FrameworkSettingConfiguration.getInstance().getSettingPath("server.mode")
                .map(settingPath -> FrameworkControl.getInstance().getProperty(settingPath))
                .orElse("off")
                .toLowerCase().equalsIgnoreCase("off")) {
            scriptJavaProcessExecutor.execute(scriptExecutionRequest);
        } else {
            scriptJavaProcessExecutor.getQueue().execute(new ScriptExecutionTask(scriptExecutionRequest, scriptJavaProcessExecutor));
        }
    }
}
