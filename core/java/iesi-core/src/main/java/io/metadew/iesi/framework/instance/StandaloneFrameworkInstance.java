package io.metadew.iesi.framework.instance;

import io.metadew.iesi.framework.configuration.FrameworkActionTypeConfiguration;
import io.metadew.iesi.framework.configuration.FrameworkConfiguration;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import io.metadew.iesi.metadata.repository.configuration.MetadataRepositoryConfiguration;
import io.metadew.iesi.runtime.ExecutorService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StandaloneFrameworkInstance extends FrameworkInstance {

    private static StandaloneFrameworkInstance INSTANCE;

    public synchronized static StandaloneFrameworkInstance getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StandaloneFrameworkInstance();
        }
        return INSTANCE;
    }

    private StandaloneFrameworkInstance() {}

    @Override
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
        ExecutorService.getInstance();
    }

}
