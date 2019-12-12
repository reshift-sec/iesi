package io.metadew.iesi.launch;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.ThreadContext;

public abstract class CLIOperation {

    private final CommandLine commandLine;

    public CLIOperation(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        this.commandLine = parser.parse(getOptions(), args);
    }

    public void execute() throws Exception {
        ThreadContext.clearAll();
        performCLIOperation();
    }
    public abstract void performCLIOperation() throws Exception;
    public abstract Options getOptions();

    public CommandLine getCommandLine() {
        return commandLine;
    }
}
