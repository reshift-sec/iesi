package io.metadew.iesi.metadata.operation;

import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import io.metadew.iesi.framework.operation.FrameworkPluginService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
public class TypeConfigurationOperation {

    public TypeConfigurationOperation() {

    }

    public static Path getTypeConfigurationFile(String dataObjectType, String typeName) {
        String configurationObject = dataObjectType + File.separator + typeName + ".json";
        Path conf = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.conf").resolve(configurationObject);

        if (Files.exists(conf)) {
            return conf;
        } else {
            return new FrameworkPluginService().getConfigFile(configurationObject);
        }
    }

    public static Path getMappingConfigurationFile(String dataObjectType, String mappingName) {
        String configurationObject = mappingName + ".json";
        Path conf = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("data.mapping").resolve(configurationObject);
        if (Files.exists(conf)) {
            return conf;
        } else {
            throw new RuntimeException(MessageFormat.format("mapping.notfound=cannot find {0}", conf));
        }
    }


}