package io.metadew.iesi.launch;

import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.launch.encrypt.EncryptCommand;
import io.metadew.iesi.launch.execute.ExecuteCommand;
import io.metadew.iesi.launch.execute.RequestCommand;
import io.metadew.iesi.launch.metadata.MetadataCommand;
import io.metadew.iesi.launch.server.ServerCommand;
import io.metadew.iesi.metadata.definition.Context;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "iesi", mixinStandardHelpOptions = true, version = "0.2.0", subcommands = {RequestCommand.class, ExecuteCommand.class, EncryptCommand.class, MetadataCommand.class, ServerCommand.class})
public class Command implements Callable<Integer> {

    @CommandLine.Option(names = "-ini", description = "initialization file used for framework startup")
    public String ini;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Command()).execute(args));
    }

    @Override
    public Integer call() {
        return 0;
    }

    public static void initFrameworkInstance(String ini) throws IOException {
        if (ini != null) {
            FrameworkInstance.getInstance().init(new FrameworkInitializationFile(ini), new FrameworkExecutionContext(new Context("script", "")));
        } else {
            FrameworkInstance.getInstance().init(new FrameworkInitializationFile(), new FrameworkExecutionContext(new Context("script", "")));
        }

    }
}
