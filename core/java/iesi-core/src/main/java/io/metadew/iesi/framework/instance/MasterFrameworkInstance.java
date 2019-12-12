package io.metadew.iesi.framework.instance;

import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;

public class MasterFrameworkInstance extends FrameworkInstance {
    private static MasterFrameworkInstance INSTANCE;

    public synchronized static MasterFrameworkInstance getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MasterFrameworkInstance();
        }
        return INSTANCE;
    }

    private MasterFrameworkInstance() {}

    @Override
    public void init(String logonType, FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext context) {

    }
}
