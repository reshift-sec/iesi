package io.metadew.iesi.launch.metadata;

import io.metadew.iesi.launch.Command;
import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@CommandLine.Command(name = "metadata", mixinStandardHelpOptions = true, version = "0.2.0",
        subcommands = {MetadataCreateCommand.class, MetadataCleanCommand.class, MetadataDropCommand.class,  MetadataLoadCommand.class})
public class MetadataCommand {

    @CommandLine.ParentCommand
    public Command command;

    @CommandLine.Option(names = "-type", arity = "1..*")
    public String[] types;

    public static void main(String[] args) {
        System.exit(new CommandLine(new MetadataCommand()).execute(args));
    }

    public static List<MetadataRepository> getMetadataRepositories(String[] types) {
        List<MetadataRepository> metadataRepositories = new ArrayList<>();
        if (Arrays.asList(types).contains("general")) {
            metadataRepositories.add(MetadataControl.getInstance().getConnectivityMetadataRepository());
            metadataRepositories.add(MetadataControl.getInstance().getDesignMetadataRepository());
            metadataRepositories.add(MetadataControl.getInstance().getResultMetadataRepository());
            metadataRepositories.add(MetadataControl.getInstance().getTraceMetadataRepository());
            metadataRepositories.add(MetadataControl.getInstance().getExecutionServerMetadataRepository());
        } else {
            if (Arrays.asList(types).contains("design")) {
                metadataRepositories.add(MetadataControl.getInstance().getDesignMetadataRepository());
            }
            if (Arrays.asList(types).contains("connectivity")) {
                metadataRepositories.add(MetadataControl.getInstance().getConnectivityMetadataRepository());
            }
            if (Arrays.asList(types).contains("trace")) {
                metadataRepositories.add(MetadataControl.getInstance().getTraceMetadataRepository());
            }
            if (Arrays.asList(types).contains("result")) {
                metadataRepositories.add(MetadataControl.getInstance().getResultMetadataRepository());
            }
            if (Arrays.asList(types).contains("execution")) {
                metadataRepositories.add(MetadataControl.getInstance().getExecutionServerMetadataRepository());
            }
        }
        return metadataRepositories;
    }
}
