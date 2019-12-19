package io.metadew.iesi.launch.metadata;

import io.metadew.iesi.launch.Command;
import io.metadew.iesi.metadata.operation.MetadataRepositoryService;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "load", mixinStandardHelpOptions = true, version = "0.2.0")
public class MetadataLoadCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private MetadataCommand metadataCommand;

    public static void main(String[] args) {
        System.exit(new CommandLine(new MetadataLoadCommand()).execute(args));
    }

    @Override
    public Integer call() {
        System.out.println("metadata.launcher.start");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        try {
            Command.initFrameworkInstance(metadataCommand.command.ini);

            MetadataRepositoryService.getInstance().load(MetadataCommand.getMetadataRepositories(metadataCommand.types));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 1;
        } finally {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("metadata.launcher.end");
        }
        return 0;
    }
}
