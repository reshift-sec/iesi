package io.metadew.iesi.framework.instance;

import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.metadata.definition.Context;
import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.metadata.repository.MetadataRepository;

public abstract class FrameworkInstance {

    public synchronized static FrameworkInstance getInstance() {
        String mode = "standalone";
        if (mode.equals("standalone")) {
            return StandaloneFrameworkInstance.getInstance();
        } else if (mode.equals("master")) {
            return MasterFrameworkInstance.getInstance();
        } else if (mode.equals("slave")) {
            return SlaveFrameworkInstance.getInstance();
        } else {
            throw new RuntimeException("no framework mode: " + mode);
        }
    }

    public void init() {
        init(new FrameworkInitializationFile(), new FrameworkExecutionContext(new Context("general", "")));
    }

    public void init(FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext context) {
        init("write", frameworkInitializationFile, context);
    }

    // public abstract void init(FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext context, String frameworkHome);

    public abstract void init(String logonType, FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext context);

    public void shutdown() {
        for (MetadataRepository metadataRepository : MetadataControl.getInstance().getMetadataRepositories()) {
            if (metadataRepository != null) {
                metadataRepository.shutdown();
            }
        }
    }

}