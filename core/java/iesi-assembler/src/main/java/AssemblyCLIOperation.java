import io.metadew.iesi.assembly.execution.AssemblyExecution;
import io.metadew.iesi.framework.configuration.FrameworkConfiguration;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.framework.execution.FrameworkControl;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public class AssemblyCLIOperation extends CLIOperation {

    private final static Options options = new Options()
            .addOption(Option.builder("assembly").desc("assemble a sandbox").build())
            .addOption(Option.builder("help").desc("print this message").build())
            .addOption(Option.builder("repository").hasArg().desc("set repository location").required().build())
            .addOption(Option.builder("development").hasArg().desc("set development location").required().build())
            .addOption(Option.builder("sandbox").hasArg().desc("set sandbox location").required().build())
            .addOption(Option.builder("instance").hasArg().desc("provide target instance").required().build())
            .addOption(Option.builder("version").hasArg().desc("provide target version").required().build())
            .addOption(Option.builder("configuration").hasArg().desc("provide target configuration").required().build())
            .addOption(Option.builder("test").desc("test assembly flag").build())
            .addOption(Option.builder("distribution").desc("distribution flag").build());
    
    public AssemblyCLIOperation(String[] args) throws ParseException {
        super(args);
    }

    @Override
    public void performCLIOperation() throws IOException {

        writeHeaderMessage();
        String development = getCommandLine().getOptionValue("development");
        System.out.println("Option -development (development) value = " + development);

        String sandbox = getCommandLine().getOptionValue("sandbox");
        System.out.println("Option -sandbox (sandbox) value = " + sandbox);

        String instance = getCommandLine().getOptionValue("instance");
        System.out.println("Option -instance (instance) value = " + instance);

        String version = getCommandLine().getOptionValue("version");
        System.out.println("Option -version (version) value = " + version);

        // FWK init
        FrameworkConfiguration frameworkConfiguration = FrameworkConfiguration.getInstance();
        frameworkConfiguration.initAssembly(repository);

        FrameworkCrypto.getInstance();

        FrameworkControl frameworkControl = FrameworkControl.getInstance();
        frameworkControl.init("assembly");

        AssemblyExecution assemblyExecution = new AssemblyExecution(repository, development, sandbox, instance,
                version, configuration, applyConfiguration, testAssembly, distribution);
        assemblyExecution.execute();

    }

    private static void writeHeaderMessage() {
        System.out.println("Invoking the assembly execution...");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();
    }

    @Override
    public Options getOptions() {
        return options;
    }
}
