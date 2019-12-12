package io.metadew.iesi.framework.operation;

import io.metadew.iesi.common.config.ConfigFile;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.framework.definition.FrameworkPlugin;
import io.metadew.iesi.framework.execution.FrameworkControl;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class FrameworkPluginService {

    private String pluginConfigurationFile;

    public FrameworkPluginService() {
    }

    public boolean verifyPlugins(String configurationToVerify) {
        boolean result = false;
        for (FrameworkPlugin frameworkPlugin : FrameworkControl.getInstance().getFrameworkPlugins()) {
            StringBuilder configurationFile = new StringBuilder();
            configurationFile.append(frameworkPlugin.getPath());
            configurationFile.append(FrameworkFolderConfiguration.getInstance().getFolderPath("metadata.conf"));
            configurationFile.append(File.separator);
            configurationFile.append(configurationToVerify);
            String filePath = FilenameUtils.normalize(configurationFile.toString());
            if (FileTools.exists(filePath)) {
                this.setPluginConfigurationFile(filePath);
                result = true;
                break;
            }
        }
        return result;
    }

    public FrameworkPlugin getFrameworkPlugin(ConfigFile configFile) {
        return new FrameworkPlugin(configFile.getProperty(FrameworkSettingConfiguration.getInstance().getSettingPath("plugin.name").get()).get().toLowerCase(),
                FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("plugins") + File.separator + configFile.getProperty(FrameworkSettingConfiguration.getInstance().getSettingPath("plugin.name").get()).get());
    }

    // Getters and setters
    public String getPluginConfigurationFile() {
        return pluginConfigurationFile;
    }

    public void setPluginConfigurationFile(String pluginConfigurationFile) {
        this.pluginConfigurationFile = pluginConfigurationFile;
    }

}