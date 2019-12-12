package io.metadew.iesi.framework.instance;

import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;

public class SlaveFrameworkInstance extends FrameworkInstance {


    private static SlaveFrameworkInstance INSTANCE;

    public synchronized static SlaveFrameworkInstance getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SlaveFrameworkInstance();
        }
        return INSTANCE;
    }

    private SlaveFrameworkInstance() {}

    @Override
    public void init(String logonType, FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext context) {

    }
}
