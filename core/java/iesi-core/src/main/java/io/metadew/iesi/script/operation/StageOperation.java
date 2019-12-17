package io.metadew.iesi.script.operation;

import io.metadew.iesi.connection.database.connection.sqlite.SqliteDatabaseConnection;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Operation to manage stage items that have been defined in the script.
 *
 * @author peter.billen
 */
public class StageOperation {

    private SqliteDatabaseConnection stageConnection;
    private String stageName;
    private Path stageFilePath;
    private boolean stageCleanup;

    //Constructors
    public StageOperation(String stageName, boolean StageCleanup) throws IOException {
        this.setStageCleanup(StageCleanup);

        Path stageFolderName = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("run.tmp").resolve("stage");
        Files.createDirectory(stageFolderName);
        this.stageFilePath = stageFolderName.resolve(stageName + "db3");
        // this.setStageConnection(new SqliteDatabaseConnection(this.getStageFilePath()));
    }

    public void doCleanup() throws IOException {
        if (this.isStageCleanup()) {
            FileTools.delete(stageFilePath);
        }
    }

    public Path getStageFilePath() {
        return stageFilePath;
    }

    public boolean isStageCleanup() {
        return stageCleanup;
    }


    public void setStageCleanup(boolean stageCleanup) {
        this.stageCleanup = stageCleanup;
    }

}