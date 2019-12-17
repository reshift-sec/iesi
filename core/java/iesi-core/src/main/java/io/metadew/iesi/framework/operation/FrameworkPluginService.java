package io.metadew.iesi.framework.operation;

import io.metadew.iesi.common.config.ConfigFile;
import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.framework.definition.FrameworkPlugin;
import io.metadew.iesi.framework.execution.FrameworkControl;

import java.nio.file.Files;
import java.nio.file.Path;

public class FrameworkPluginService {

    public FrameworkPluginService() {
    }

    public FrameworkPlugin getFrameworkPlugin(ConfigFile configFile) {
        return new FrameworkPlugin(configFile.getProperty(FrameworkSettingConfiguration.getInstance().getSettingPath("plugin.name").get()).get().toLowerCase(),
                FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("plugins").resolve(configFile.getProperty(FrameworkSettingConfiguration.getInstance().getSettingPath("plugin.name").get()).get()));
    }

    public Path getConfigFile(String configurationObject) {
        return FrameworkControl.getInstance().getFrameworkPlugins().stream()
                .map(frameworkPlugin ->frameworkPlugin.getPath().resolve(FrameworkFolderConfiguration.getInstance().getFolderPath("metadata.conf")).resolve(configurationObject))
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find configuration object" + configurationObject));
    }
}