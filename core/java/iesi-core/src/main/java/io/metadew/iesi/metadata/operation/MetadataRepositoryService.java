package io.metadew.iesi.metadata.operation;

import io.metadew.iesi.common.text.ParsingTools;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import io.metadew.iesi.metadata.configuration.script.trace.ScriptTraceConfiguration;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetadataRepositoryService {

    private static final PathMatcher JSON_YML_MATCHER = FileSystems.getDefault().getPathMatcher("regex:(.*\\.json)|(.*\\.yml)");

    private static final Logger LOGGER = LogManager.getLogger();
    private static MetadataRepositoryService INSTANCE;


    public synchronized static MetadataRepositoryService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetadataRepositoryService();
        }
        return INSTANCE;
    }

    private MetadataRepositoryService() {
    }


    public void load(List<MetadataRepository> metadataRepositories) throws IOException {
        // load(metadataRepositories, "");
        Path inputFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.new");
        Path workFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.work");
        Path errorFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.error");
        Path archiveFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.done");

        loadConfigurationSelection(metadataRepositories, inputFolder, workFolder, archiveFolder, errorFolder, Files.walk(inputFolder)
                .filter(JSON_YML_MATCHER::matches)
                .map(Path::toFile)
                .collect(Collectors.toList()));
    }

    public void load(List<MetadataRepository> metadataRepositories, String input) throws IOException {
        LOGGER.info("metadata.load.start");

        // Folder definition
        Path inputFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.new");
        Path workFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.work");
        Path errorFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.error");
        Path archiveFolder = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.in.done");
            if (ParsingTools.isRegexFunction(input)) {
                loadConfigurationSelection(metadataRepositories, inputFolder, workFolder, archiveFolder, errorFolder,
                        Files.walk(inputFolder)
                                .filter(FileSystems.getDefault().getPathMatcher("regex:" + ParsingTools.getRegexFunctionValue(input))::matches)
                                .map(Path::toFile)
                                .collect(Collectors.toList())
                );
            } else {
                loadConfigurationSelection(metadataRepositories, inputFolder, workFolder, archiveFolder, errorFolder,
                        Stream.of(input.split(","))
                                .map(Paths::get)
                                .map(Path::toFile)
                                .collect(Collectors.toList()));
            }

    }

    // Load entire folder
    private void loadConfigurationSelection(List<MetadataRepository> metadataRepositories, Path inputFolder, Path workFolder, Path archiveFolder,
                                            Path errorFolder, List<File> files) throws IOException {
        for (final File file : files) {
            this.loadConfigurationFile(metadataRepositories, file, inputFolder, workFolder, archiveFolder, errorFolder);
        }
    }

    private void loadConfigurationFile(List<MetadataRepository> metadataRepositories, File file, Path inputFolder, Path workFolder, Path archiveFolder,
                                       Path errorFolder) throws IOException {

        UUID uuid = UUID.randomUUID();

        FileTools.copyFromFileToFile(inputFolder.resolve(file.getName()), workFolder.resolve(file.getName()));
        FileTools.delete(inputFolder.resolve(file.getName()));


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");

        File workFile = new File(FilenameUtils.normalize(workFolder + File.separator + file.getName()));
        if (!workFile.isDirectory()) {
            try {
                LOGGER.info("metadata.file=" + file.getName());
                DataObjectOperation dataObjectOperation = new DataObjectOperation(workFile.getAbsolutePath());
                dataObjectOperation.saveToMetadataRepository(metadataRepositories);

                // Move file to archive folder
                String archiveFileName = dateFormat.format(new Date()) + "-" + timeFormat.format(new Date()) + "-"
                        + uuid + "-" + workFile.getName();
                FileTools.copyFromFileToFile(workFile.getAbsolutePath(),
                        FilenameUtils.normalize(archiveFolder + File.separator + archiveFileName));
                FileTools.delete(workFile.getAbsolutePath());

            } catch (Exception e) {

                String errorFileName = dateFormat.format(new Date()) + "-" + timeFormat.format(new Date()) + "-"
                        + uuid + "-" + file.getName();
                FileTools.copyFromFileToFile(workFile.getAbsolutePath(),
                        FilenameUtils.normalize(errorFolder + File.separator + errorFileName));
                FileTools.delete(workFile.getAbsolutePath());

            }
        }

    }

}