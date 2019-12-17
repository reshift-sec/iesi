package io.metadew.iesi.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.framework.definition.FrameworkFolder;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.operation.DataObjectOperation;
import org.apache.logging.log4j.ThreadContext;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FrameworkFolderConfiguration {

    private Map<String, FrameworkFolder> folderMap;
    private static FrameworkFolderConfiguration INSTANCE;

    public synchronized static FrameworkFolderConfiguration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FrameworkFolderConfiguration();
        }
        return INSTANCE;
    }

    private FrameworkFolderConfiguration() {}

    public void init(String solutionHome) {
        init(Paths.get(solutionHome));
    }

    public void init(Path solutionHome) {
        this.folderMap = new HashMap<>();

        Path initFilePath = solutionHome.resolve("sys").resolve("init").resolve("FrameworkFolders.json");
        DataObjectOperation dataObjectOperation = new DataObjectOperation(initFilePath);
        ObjectMapper objectMapper = new ObjectMapper();
        for (DataObject dataObject: dataObjectOperation.getDataObjects()) {
            if (dataObject.getType().equalsIgnoreCase("frameworkfolder")) {
                FrameworkFolder frameworkFolder = objectMapper.convertValue(dataObject.getData(), FrameworkFolder.class);
                Path folderPath = solutionHome.resolve(frameworkFolder.getPath());
                frameworkFolder.setAbsolutePath(folderPath);
                folderMap.put(frameworkFolder.getName(), frameworkFolder);
            }
        }
        ThreadContext.put("location", getFolderAbsolutePath("logs").toString());
    }
    public Path getFolderAbsolutePath(String key) {
        return folderMap.get(key).getAbsolutePath();
    }

    public String getFolderPath(String key) {
        return folderMap.get(key).getPath();
    }

}