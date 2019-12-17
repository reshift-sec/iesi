package io.metadew.iesi.launch;

import io.metadew.iesi.common.config.ConfigFile;
import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.framework.execution.FrameworkRuntime;
import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.metadata.definition.Context;
import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.metadata.operation.MetadataRepositoryOperation;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import io.metadew.iesi.metadata.repository.configuration.MetadataRepositoryConfiguration;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MetadataCLIOperation extends CLIOperation {
    private final static Options options = new Options()
            .addOption(Option.builder("metadata").desc("perform metadata operations").build())
            .addOption(new Option("help", "print this message"))
            .addOption(Option.builder("ini").hasArg().desc("define the initialization file").build())
            .addOption(Option.builder("type").hasArg().desc("define the type of metadata repository").required().build())
            .addOption(Option.builder("config").hasArg().desc("define the metadata repository config").build())
            .addOption(Option.builder("backup").desc("create a backup of the entire metadata repository").build())
            .addOption(Option.builder("restore").desc("restore a backup of the metadata repository").build())
            .addOption(Option.builder("path").hasArg().desc("path to be used to for backup or restore").build())
            .addOption(Option.builder("drop").desc("drop all metadata tables in the metadata repository").build())
            .addOption(Option.builder("create").desc("create all metadata tables in the metadata repository").build())
            .addOption(Option.builder("clean").desc("clean all tables in the metadata repository").build())
            .addOption(Option.builder("load").desc("load metadata file from the input folder into the metadata repository").build())
            .addOption(Option.builder("ddl").desc("generate ddl output instead of execution in the metadata repository, to be combined with options: create, drop").build())
            .addOption(Option.builder("files").hasArg().desc(
                    "filename(s) to load from the input folder into the metadata repository\n" +
                            "Following options are possible:\n" +
                            "-(1) a single file name including extension\n" +
                            "--Example: Script.json\n" +
                            "-(2) list of files separated by commas \n" +
                            "--Example: Script1.json,Script2.json\n" +
                            "-(3) a regular expression written as function =regex([your expression])\n" +
                            "--Example: =regex(.+\\json) > this will load all files").build())
            .addOption(Option.builder("exit").hasArg().desc("define if an explicit exit is required").build());
    
    
    public MetadataCLIOperation(String[] args) throws ParseException {
        super(args);
    }

    @Override
    public void performCLIOperation() throws Exception {
        if (getCommandLine().hasOption("help")) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("[command]", options);
            System.exit(0);
        }

        // Define the exit behaviour
        boolean exit = !getCommandLine().hasOption("exit") || getCommandLine().getOptionValue("exit").equalsIgnoreCase("y") || getCommandLine().getOptionValue("exit").equalsIgnoreCase("true");

        // Create the framework instance
        FrameworkInitializationFile frameworkInitializationFile = new FrameworkInitializationFile();
        if (getCommandLine().hasOption("ini")) {
            System.out.println("Option -ini (ini) value = " + getCommandLine().getOptionValue("ini"));
            frameworkInitializationFile.setName(getCommandLine().getOptionValue("ini"));
        }

        FrameworkInstance.getInstance().init(frameworkInitializationFile, new FrameworkExecutionContext(new Context("metadata", "")));

        MetadataRepositoryOperation metadataRepositoryOperation = new MetadataRepositoryOperation();
        List<MetadataRepository> metadataRepositories = new ArrayList<>();

        System.out.println("Option -type (type) value = " + getCommandLine().getOptionValue("type"));
        String type = getCommandLine().getOptionValue("type");


        if (getCommandLine().hasOption("config")) {
            String config = getCommandLine().getOptionValue("config");

            ConfigFile configFile = FrameworkControl.getInstance().getConfigFile("keyvalue",
                    FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("conf").resolve(config).toString());

            metadataRepositories = new MetadataRepositoryConfiguration(configFile).toMetadataRepositories();

            // metadataRepositories.addAll(metadataRepositories);

        } else {
            switch (type) {
                case "catalog":
                    metadataRepositories.add(MetadataControl.getInstance().getCatalogMetadataRepository());
                    break;
                case "connectivity":
                    metadataRepositories.add(MetadataControl.getInstance().getConnectivityMetadataRepository());
                    break;
                case "control":
                    metadataRepositories.add(MetadataControl.getInstance().getControlMetadataRepository());
                    break;
                case "design":
                    metadataRepositories.add(MetadataControl.getInstance().getDesignMetadataRepository());
                    break;
                case "result":
                    metadataRepositories.add(MetadataControl.getInstance().getResultMetadataRepository());
                    break;
                case "trace":
                    metadataRepositories.add(MetadataControl.getInstance().getTraceMetadataRepository());
                    break;
                case "execution_server":
                    metadataRepositories.add(MetadataControl.getInstance().getExecutionServerMetadataRepository());
                    break;
                case "general":
                    metadataRepositories.add(MetadataControl.getInstance().getCatalogMetadataRepository());
                    metadataRepositories.add(MetadataControl.getInstance().getConnectivityMetadataRepository());
                    metadataRepositories.add(MetadataControl.getInstance().getControlMetadataRepository());
                    metadataRepositories.add(MetadataControl.getInstance().getDesignMetadataRepository());
                    metadataRepositories.add(MetadataControl.getInstance().getResultMetadataRepository());
                    metadataRepositories.add(MetadataControl.getInstance().getTraceMetadataRepository());
                    metadataRepositories.add(MetadataControl.getInstance().getExecutionServerMetadataRepository());
                    break;
                default:
                    System.out.println("Unknown Option -type (type) = " + type);
                    endLauncher(1, true);
            }
        }

        // Drop
        if (getCommandLine().hasOption("drop")) {
            for (MetadataRepository metadataRepository : metadataRepositories) {

                writeHeaderMessage();
                System.out.println("Option -drop (drop) selected");
                System.out.println();
                metadataRepository.dropAllTables();
                writeFooterMessage();
            }
        }

        // DDL
        if (getCommandLine().hasOption("ddl")) {
            for (MetadataRepository metadataRepository : metadataRepositories) {
                System.out.println(metadataRepository.generateDDL());
            }
        }

        // Create
        if (getCommandLine().hasOption("create")) {
            for (MetadataRepository metadataRepository : metadataRepositories) {
                writeHeaderMessage();
                System.out.println("Option -create (create) selected");
                System.out.println();
                System.out.println(MessageFormat.format("Creating metadata repository {0}", metadataRepository.getCategory()));
                metadataRepository.createAllTables();
                writeFooterMessage();
            }
        }

        // clean
        if (getCommandLine().hasOption("clean")) {
            for (MetadataRepository metadataRepository : metadataRepositories) {
                writeHeaderMessage();
                System.out.println("Option -clean (clean) selected");
                System.out.println();
                metadataRepository.cleanAllTables();
                writeFooterMessage();
            }

        }

        // load
        if (getCommandLine().hasOption("load")) {
            writeHeaderMessage();
            System.out.println("Option -load (load) selected");
            System.out.println();
            if (getCommandLine().hasOption("files")) {
                String files = "";
                files = getCommandLine().getOptionValue("files");
                metadataRepositoryOperation.loadMetadataRepository(metadataRepositories, files);
            } else {
                metadataRepositoryOperation.loadMetadataRepository(metadataRepositories);
            }
            writeFooterMessage();
        }

        System.out.println();
        System.out.println("metadata.launcher.end");
        FrameworkInstance.getInstance().shutdown();
        endLauncher(0, exit);
    }

    private static void endLauncher(int status, boolean exit) {
        FrameworkRuntime.getInstance().terminate();
        if (exit) {
            System.exit(status);
        }
    }

    private static void writeHeaderMessage() {
        System.out.println("metadata.launcher.start");
        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    private static void writeFooterMessage() {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }


    @Override
    public Options getOptions() {
        return options;
    }
}
