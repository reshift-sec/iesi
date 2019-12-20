package io.metadew.iesi.framework.instance;

import io.metadew.iesi.framework.configuration.FrameworkActionTypeConfiguration;
import io.metadew.iesi.framework.configuration.FrameworkConfiguration;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.metadata.definition.Context;
import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import io.metadew.iesi.metadata.repository.configuration.MetadataRepositoryConfiguration;
import io.metadew.iesi.runtime.ExecutorService;
import io.metadew.iesi.runtime.script.ScriptExecutionRequestListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public void init() throws IOException {
        init(new FrameworkInitializationFile(), new FrameworkExecutionContext(new Context("general", "")));
    }

    public void init(FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext context) throws IOException {
        init("write", frameworkInitializationFile, context);
    }


    public void init(String logonType, FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext context) throws IOException {
        // Get the framework configuration
        FrameworkConfiguration frameworkConfiguration = FrameworkConfiguration.getInstance();
        frameworkConfiguration.init();

        FrameworkCrypto.getInstance();

        // Set appropriate initialization file
        if (frameworkInitializationFile.getName().trim().isEmpty()) {
            frameworkInitializationFile = new FrameworkInitializationFile(frameworkConfiguration.getFrameworkCode() + "-conf.ini");
        }

        // Prepare configuration and shared Metadata
        FrameworkControl frameworkControl = FrameworkControl.getInstance();
        frameworkControl.init(frameworkInitializationFile);

        FrameworkActionTypeConfiguration.getInstance().setActionTypesFromPlugins(frameworkControl.getFrameworkPlugins());
        List<MetadataRepository> metadataRepositories = new ArrayList<>();

        for (MetadataRepositoryConfiguration metadataRepositoryConfiguration : frameworkControl.getMetadataRepositoryConfigurations()) {
            metadataRepositories.addAll(metadataRepositoryConfiguration.toMetadataRepositories());

        }
        MetadataControl.getInstance().init(metadataRepositories);

        FrameworkExecution.getInstance().init(context);
        // TODO: move Executor (Request to separate module)
        ScriptExecutionRequestListener.getInstance();
        ExecutorService.getInstance();
    }

    public void shutdown() {
        for (MetadataRepository metadataRepository : MetadataControl.getInstance().getMetadataRepositories()) {
            if (metadataRepository != null) {
                metadataRepository.shutdown();
            }
        }
    }

}