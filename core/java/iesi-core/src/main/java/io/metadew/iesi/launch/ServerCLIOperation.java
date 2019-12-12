package io.metadew.iesi.launch;

import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.metadata.definition.Context;
import io.metadew.iesi.runtime.ExecutionRequestListener;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ServerCLIOperation extends CLIOperation {

    private final static Options options = new Options()
            .addOption(Option.builder("help").desc("print this message").build())
            .addOption(Option.builder("ini").hasArg().desc("define the initialization file").build());
    
    public ServerCLIOperation(String[] args) throws ParseException {
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

        if (getCommandLine().hasOption("ini")) {
            FrameworkInstance.getInstance().init(new FrameworkInitializationFile(getCommandLine().getOptionValue("ini")),
                    new FrameworkExecutionContext(new Context("server", "")));
        } else {
            FrameworkInstance.getInstance().init(new FrameworkInitializationFile(),
                    new FrameworkExecutionContext(new Context("server", "")));
        }
        FrameworkInstance frameworkInstance = FrameworkInstance.getInstance();
        ExecutionRequestListener executionRequestListener= new ExecutionRequestListener();
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                executionRequestListener.shutdown();
                frameworkInstance.shutdown();
                mainThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        new Thread(executionRequestListener).start();
    }

    @Override
    public Options getOptions() {
        return options;
    }
}
