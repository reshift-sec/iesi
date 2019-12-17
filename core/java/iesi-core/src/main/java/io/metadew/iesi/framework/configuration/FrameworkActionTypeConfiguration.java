package io.metadew.iesi.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.framework.definition.FrameworkPlugin;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.definition.action.type.ActionType;
import io.metadew.iesi.metadata.operation.DataObjectOperation;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkActionTypeConfiguration {

    private Map<String, ActionType> actionTypeMap;

    private static final Logger LOGGER = LogManager.getLogger();

    private static FrameworkActionTypeConfiguration INSTANCE;

    public synchronized static FrameworkActionTypeConfiguration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FrameworkActionTypeConfiguration();
        }
        return INSTANCE;
    }

    private FrameworkActionTypeConfiguration() {}

    public void init(FrameworkFolderConfiguration frameworkFolderConfiguration) {
        actionTypeMap = new HashMap<>();

        Path initFilePath = frameworkFolderConfiguration.getFolderAbsolutePath("metadata.conf").resolve("ActionTypes.json");
        DataObjectOperation dataObjectOperation = new DataObjectOperation(initFilePath);

        ObjectMapper objectMapper = new ObjectMapper();
        for (DataObject dataObject : dataObjectOperation.getDataObjects()) {
            if (dataObject.getType().equalsIgnoreCase("actiontype")) {
                ActionType actionType = objectMapper.convertValue(dataObject.getData(), ActionType.class);
                actionTypeMap.put(actionType.getName().toLowerCase(), actionType);
            }
        }
    }

    public void setActionTypesFromPlugins(List<FrameworkPlugin> frameworkPlugins) {
        for (FrameworkPlugin frameworkPlugin : frameworkPlugins) {
            Path initFilePath = frameworkPlugin.getPath() .resolve(FrameworkFolderConfiguration.getInstance().getFolderPath("metadata.conf")).resolve(File.separator).resolve("ActionTypes.json");

            if (Files.exists(initFilePath)) {
                DataObjectOperation dataObjectOperation = new DataObjectOperation(initFilePath);
                ObjectMapper objectMapper = new ObjectMapper();
                for (DataObject dataObject : dataObjectOperation.getDataObjects()) {
                    if (dataObject.getType().equalsIgnoreCase("actiontype")) {
                        ActionType actionType = objectMapper.convertValue(dataObject.getData(), ActionType.class);
                        if (actionTypeMap.containsKey(actionType.getName().toLowerCase())) {
                            LOGGER.warn("item already present - skipping " + actionType.getName());
                        } else {
                            actionTypeMap.put(actionType.getName().toLowerCase(), actionType);
                        }
                    }
                }
            }
        }
    }

}