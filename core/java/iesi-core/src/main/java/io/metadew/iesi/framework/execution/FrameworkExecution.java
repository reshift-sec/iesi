package io.metadew.iesi.framework.execution;

import org.apache.logging.log4j.ThreadContext;

public class FrameworkExecution {

    private FrameworkExecutionContext frameworkExecutionContext;

    private static FrameworkExecution INSTANCE;

    public synchronized static FrameworkExecution getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FrameworkExecution();
        }
        return INSTANCE;
    }

    private FrameworkExecution() {}

    public void init() {
        init(new FrameworkExecutionContext());
    }

    public void init(FrameworkExecutionContext frameworkExecutionContext) {
            this.frameworkExecutionContext = frameworkExecutionContext;
        ThreadContext.put("context.name", frameworkExecutionContext.getContext().getName());
        ThreadContext.put("context.scope", frameworkExecutionContext.getContext().getScope());
        FrameworkRuntime.getInstance().init();
    }

    public FrameworkExecutionContext getFrameworkExecutionContext() {
        return frameworkExecutionContext;
    }

}