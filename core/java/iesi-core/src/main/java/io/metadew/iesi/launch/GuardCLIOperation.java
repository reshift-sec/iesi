package io.metadew.iesi.launch;


import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.guard.execution.GuardExecution;
import io.metadew.iesi.metadata.definition.Context;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class GuardCLIOperation extends CLIOperation {

    private final static Options options = new Options()
            .addOption(new Option("help", "print this message"))
            .addOption(new Option("user", true, "define the user name"))
            .addOption(new Option("create", "create a new user"))
            .addOption(new Option("password", "reset a user password"))
            .addOption(new Option("active", true, "switch a user to (in)active"))
            .addOption(new Option("locked", true, "(un)block a user"))
            .addOption(new Option("reset", "resets the individual login fail counter"));

    public GuardCLIOperation(String[] args) throws ParseException {
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

        // Calling the launch controller
        System.out.println();
        System.out.println("guard.launcher.start");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        // Create the framework instance
        FrameworkInstance.getInstance().init(new FrameworkInitializationFile(),
                new FrameworkExecutionContext(new Context("guard", "user")));


        String userName = "";
        String active = "";
        String locked = "";
        if (getCommandLine().hasOption("user")) {
            userName = getCommandLine().getOptionValue("user");
            System.out.println("Option -user (user) value = " + userName);

            if (getCommandLine().hasOption("create")) {
                GuardExecution guardExecution = new GuardExecution();
                guardExecution.createUser(userName);
            }

            if (getCommandLine().hasOption("password")) {
                GuardExecution guardExecution = new GuardExecution();
                guardExecution.resetPassword(userName);
            }

            if (getCommandLine().hasOption("active")) {
                active = getCommandLine().getOptionValue("active");
                GuardExecution guardExecution = new GuardExecution();
                guardExecution.updateActive(userName, active);
            }

            if (getCommandLine().hasOption("locked")) {
                locked = getCommandLine().getOptionValue("locked");
                GuardExecution guardExecution = new GuardExecution();
                guardExecution.updateLocked(userName, locked);
            }

            if (getCommandLine().hasOption("reset")) {
                GuardExecution guardExecution = new GuardExecution();
                guardExecution.resetIndividualLoginFails(userName);
            }

        }
        FrameworkInstance.getInstance().shutdown();
    }

    @Override
    public Options getOptions() {
        return options;
    }
}
