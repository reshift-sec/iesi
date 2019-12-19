package io.metadew.iesi.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.metadata.definition.Context;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.test.launch.LaunchArgument;
import io.metadew.iesi.test.launch.LaunchItem;
import io.metadew.iesi.test.launch.LaunchItemOperation;
import io.metadew.iesi.test.launch.Launcher;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActionsTest {

    public static void main(String[] args) throws Exception {

        Options options = new Options()
                .addOption(Option.builder("repository").hasArg().required()
                        .desc("Absolute location of the iesi repository").build())
                .addOption(Option.builder("sandbox").hasArg().required().desc("Absolute location of the iesi sandbox")
                        .build())
                .addOption(Option.builder("ini").hasArg().desc("ini file of the iesi instance").build());


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String repository = cmd.getOptionValue("repository");
        String sandbox = cmd.getOptionValue("sandbox");

        String testDataHome = repository + File.separator + "test" + File.separator + "data";
        String testDockerHome = repository + File.separator + "test" + File.separator + "docker";
        String testFwkConfigurationHome = repository + File.separator + "test" + File.separator + "metadata"
                + File.separator + "conf" + File.separator + "fwk";
        String testLaunchConfigurationHome = repository + File.separator + "test" + File.separator + "metadata"
                + File.separator + "conf" + File.separator + "launch";
        String testSetupConfigurationHome = repository + File.separator + "test" + File.separator + "metadata"
                + File.separator + "conf" + File.separator + "setup";
        String testDefConfigurationHome = repository + File.separator + "test" + File.separator + "metadata"
                + File.separator + "conf" + File.separator + "def";
        String actionsTestConfigurationHome = repository + File.separator + "test" + File.separator + "metadata"
                + File.separator + "conf" + File.separator + "actions";
        String instructionsTestConfigurationHome = repository + File.separator + "test" + File.separator
                + "metadata" + File.separator + "conf" + File.separator + "instructions";
        String connectionsTestConfigurationHome = repository + File.separator + "test" + File.separator
                + "metadata" + File.separator + "conf" + File.separator + "connections";

        String versionHomeConfFolder = sandbox + File.separator + "conf";
        String dataFolder = sandbox + File.separator + "data";
        String testDataFolder = sandbox + File.separator + "data" + File.separator + "iesi-test";
        String frameworkTestDataFolder = testDataFolder + File.separator + "fwk";
        String setupTestConfDataFolder = frameworkTestDataFolder + File.separator + "setup";
        String actionsTestConfDataFolder = frameworkTestDataFolder + File.separator + "actions";
        String instructionsTestConfDataFolder = frameworkTestDataFolder + File.separator + "instructions";
        String connectionsTestConfDataFolder = frameworkTestDataFolder + File.separator + "connections";
        String actionsTestDefDataFolder = frameworkTestDataFolder + File.separator + "def";
        String fwkTestDataFolder = frameworkTestDataFolder + File.separator + "data";

        String dockerTestDataFolder = testDataFolder + File.separator + "docker";

        String metadataInNewFolder = sandbox + File.separator + "metadata" + File.separator + "in"
                + File.separator + "new";

        FolderTools.createFolder(testDataFolder);
        FolderTools.deleteFolder(frameworkTestDataFolder, true);
        FolderTools.deleteFolder(dockerTestDataFolder, true);
        FolderTools.createFolder(frameworkTestDataFolder);
        FolderTools.createFolder(setupTestConfDataFolder);
        FolderTools.createFolder(actionsTestConfDataFolder);
        FolderTools.createFolder(instructionsTestConfDataFolder);
        FolderTools.createFolder(connectionsTestConfDataFolder);
        FolderTools.createFolder(actionsTestDefDataFolder);
        FolderTools.createFolder(fwkTestDataFolder);

        FolderTools.createFolder(dockerTestDataFolder);

        // Docker configuration
        FolderTools.copyFromFolderToFolder(testDockerHome + File.separator + "linux-ssh",
                dockerTestDataFolder + File.separator + "linux-ssh", true);
        FolderTools.copyFromFolderToFolder(testSetupConfigurationHome, setupTestConfDataFolder, false);

        // Fwk configuration
        FolderTools.copyFromFolderToFolder(testFwkConfigurationHome, versionHomeConfFolder, false);

        // Data
        FolderTools.copyFromFolderToFolder(testDataHome, fwkTestDataFolder, true);
        // Temp solution for datasets and mappings
        // TODO: create relative path for datasets and mappings
        FolderTools.createFolder(dataFolder + File.separator + "datasets");
        FolderTools.copyFromFolderToFolder(testDataHome + File.separator + "datasets",
                dataFolder + File.separator + "datasets", true);
        FolderTools.createFolder(dataFolder + File.separator + "mapping");
        FolderTools.copyFromFolderToFolder(testDataHome + File.separator + "mapping",
                dataFolder + File.separator + "mapping", true);

        // Definitions
        FolderTools.copyFromFolderToFolder(testDefConfigurationHome + File.separator + "connections",
                actionsTestDefDataFolder, false);
        FileTools.delete(actionsTestConfDataFolder + File.separator + ".gitkeep");
        FolderTools.copyFromFolderToFolder(testDefConfigurationHome + File.separator + "environments",
                actionsTestDefDataFolder, false);
        FileTools.delete(actionsTestConfDataFolder + File.separator + ".gitkeep");

        // Configurations
        FolderTools.copyFromFolderToFolder(actionsTestConfigurationHome, actionsTestConfDataFolder, false);
        FileTools.delete(actionsTestConfDataFolder + File.separator + ".gitkeep");
        FolderTools.copyFromFolderToFolder(instructionsTestConfigurationHome, instructionsTestConfDataFolder,
                false);
        FileTools.delete(actionsTestConfDataFolder + File.separator + ".gitkeep");
        FolderTools.copyFromFolderToFolder(connectionsTestConfigurationHome, connectionsTestConfDataFolder, false);
        FileTools.delete(connectionsTestConfDataFolder + File.separator + ".gitkeep");

        if (cmd.hasOption("ini")) {
            // Create framework instance
            System.out.println("Option -ini (ini) value = " + cmd.getOptionValue("ini"));
            FrameworkInstance.getInstance().init(new FrameworkInitializationFile(cmd.getOptionValue("ini")),
                    new FrameworkExecutionContext(new Context("script", "")));
        } else {
            FrameworkInstance.getInstance().init(new FrameworkInitializationFile(),
                    new FrameworkExecutionContext(new Context("script", "")));
        }

        // Create repository
        List<LaunchArgument> metadataCreateArgs = new ArrayList<>();
        LaunchArgument ini = new LaunchArgument(true, "-ini", cmd.getOptionValue("ini", "iesi-test.ini"));
        metadataCreateArgs.add(ini);
        LaunchArgument metadata = new LaunchArgument(false, "metadata", "");
        metadataCreateArgs.add(metadata);
        LaunchArgument type = new LaunchArgument(true, "-type", "general");
        metadataCreateArgs.add(type);
        LaunchArgument create = new LaunchArgument(false, "create", "");
        metadataCreateArgs.add(create);
        Launcher.execute(metadataCreateArgs);

        File[] confs = ArrayUtils.addAll(
                FolderTools.getFilesInFolder(actionsTestDefDataFolder, "regex", ".+\\.yml"),
                FolderTools.getFilesInFolder(instructionsTestConfDataFolder, "regex", ".+\\.yml"));

        List<LaunchArgument> inputMetadataArgs = new ArrayList<>();
        inputMetadataArgs.add(ini);
        inputMetadataArgs.add(metadata);
        inputMetadataArgs.add(type);
        LaunchArgument load = new LaunchArgument(false, "load", "");
        inputMetadataArgs.add(load);

        for (final File conf : confs) {
            FileTools.copyFromFileToFile(conf.getAbsolutePath(),
                    metadataInNewFolder + File.separator + conf.getName());
            Launcher.execute(inputMetadataArgs);
        }

        // Load setup scripts
        confs = FolderTools.getFilesInFolder(setupTestConfDataFolder, "regex", ".+\\.yml");

//        inputMetadataArgs.add(ini);
//        inputMetadataArgs.add(load);
//        inputMetadataArgs.add(type);

        for (final File conf : confs) {
            FileTools.copyFromFileToFile(conf.getAbsolutePath(),
                    metadataInNewFolder + File.separator + conf.getName());
            Launcher.execute(inputMetadataArgs);
        }

        // Load action tests
        confs = FolderTools.getFilesInFolder(actionsTestConfDataFolder, "regex", ".+\\.yml");

        for (final File conf : confs) {
            FileTools.copyFromFileToFile(conf.getAbsolutePath(),
                    metadataInNewFolder + File.separator + conf.getName());
            Launcher.execute(inputMetadataArgs);
        }

        // Load connections tests
        confs = FolderTools.getFilesInFolder(connectionsTestConfDataFolder, "regex", ".+\\.yml");

        for (final File conf : confs) {
            FileTools.copyFromFileToFile(conf.getAbsolutePath(),
                    metadataInNewFolder + File.separator + conf.getName());
            Launcher.execute(inputMetadataArgs);
        }

        // ------------

        List<LaunchArgument> scriptInputArgs = new ArrayList<>();
        scriptInputArgs.add(ini);
        LaunchArgument execute = new LaunchArgument(false, "execute", "");
        scriptInputArgs.add(execute);
        LaunchArgument env = new LaunchArgument(true, "-env", "iesi-test");
        scriptInputArgs.add(env);
        LaunchArgument script;
        ObjectMapper objectMapper = new ObjectMapper();

        // Run initializations
        LaunchItemOperation launchInitializationOperation = new LaunchItemOperation(
                testLaunchConfigurationHome + File.separator + "initializations.json");
        for (DataObject dataObject : launchInitializationOperation.getDataObjects()) {
            LaunchItem launchItem = objectMapper.convertValue(dataObject.getData(), LaunchItem.class);
            script = new LaunchArgument(true, "-name", launchItem.getScript());
            scriptInputArgs.add(script);


            // Parameter list
            LaunchArgument paramList = null;
            if (launchItem.getParameterList() != null && !launchItem.getParameterList().trim().isEmpty()) {
                paramList = new LaunchArgument(true, "-parameters", launchItem.getParameterList());
                scriptInputArgs.add(paramList);
            }

            Launcher.execute(scriptInputArgs);

            scriptInputArgs.remove(script);
            scriptInputArgs.remove(paramList);
        }

        // Run action tests
        LaunchItemOperation actionLaunchItemOperation = new LaunchItemOperation(
                testLaunchConfigurationHome + File.separator + "actions.json");
        for (DataObject dataObject : actionLaunchItemOperation.getDataObjects()) {
            LaunchItem launchItem = objectMapper.convertValue(dataObject.getData(), LaunchItem.class);
            script = new LaunchArgument(true, "-name", launchItem.getScript());
            scriptInputArgs.add(script);

            // Parameter list
            LaunchArgument paramList = null;
            if (launchItem.getParameterList() != null && !launchItem.getParameterList().trim().isEmpty()) {
                paramList = new LaunchArgument(true, "-parameters", launchItem.getParameterList());
                scriptInputArgs.add(paramList);
            }

            Launcher.execute(scriptInputArgs);

            scriptInputArgs.remove(script);
            scriptInputArgs.remove(paramList);
        }

        // Run action tests
        LaunchItemOperation instructionLaunchItemOperation = new LaunchItemOperation(
                testLaunchConfigurationHome + File.separator + "instructions.json");
        for (DataObject dataObject : instructionLaunchItemOperation.getDataObjects()) {
            LaunchItem launchItem = objectMapper.convertValue(dataObject.getData(), LaunchItem.class);
            script = new LaunchArgument(true, "-name", launchItem.getScript());
            scriptInputArgs.add(script);

            // Parameter list
            LaunchArgument paramList = null;
            if (launchItem.getParameterList() != null && !launchItem.getParameterList().trim().isEmpty()) {
                paramList = new LaunchArgument(true, "-parameters", launchItem.getParameterList());
                scriptInputArgs.add(paramList);
            }

            Launcher.execute(scriptInputArgs);

            scriptInputArgs.remove(script);
            scriptInputArgs.remove(paramList);
        }

        // Run terminations
        LaunchItemOperation launchTerminationOperation = new LaunchItemOperation(
                testLaunchConfigurationHome + File.separator + "terminations.json");
        for (DataObject dataObject : launchTerminationOperation.getDataObjects()) {
            LaunchItem launchItem = objectMapper.convertValue(dataObject.getData(), LaunchItem.class);
            script = new LaunchArgument(true, "-name", launchItem.getScript());
            scriptInputArgs.add(script);

            // Parameter list
            LaunchArgument paramList = null;
            if (launchItem.getParameterList() != null && !launchItem.getParameterList().trim().isEmpty()) {
                paramList = new LaunchArgument(true, "-parameters", launchItem.getParameterList());
                scriptInputArgs.add(paramList);
            }

            Launcher.execute(scriptInputArgs);

            scriptInputArgs.remove(script);
            scriptInputArgs.remove(paramList);
        }

        System.exit(0);
    }
}