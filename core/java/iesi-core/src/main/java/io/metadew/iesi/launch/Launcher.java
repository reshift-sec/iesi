package io.metadew.iesi.launch;

import org.apache.commons.cli.*;

import java.util.Arrays;

public class Launcher {

    public static void main(String[] args) throws Exception {

        Options options = new Options()
                .addOption(Option.builder("assembly").desc("assemble a sandbox").build())
                .addOption(Option.builder("encrypt").desc("encrypt according the IESI configuration").build())
                .addOption(Option.builder("script").desc("launch a script").build())
                .addOption(Option.builder("metadata").desc("perform metadata operations").build())
                .addOption(Option.builder("server").desc("start IESI in server mode").build())
                .addOption(Option.builder("guard").desc("start IESI in server mode").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args, true);

        if (line.hasOption("assembly")) {
            new AssemblyCLIOperation(args).execute();
        } else if (line.hasOption("encrypt")) {
            new EncryptionCLIOperation(args).execute();
        } else if (line.hasOption("script")) {
            new ScriptCLIOperation(args).execute();
        } else if (line.hasOption("metadata")) {
            new MetadataCLIOperation(args).execute();
        } else if (line.hasOption("server")) {
            new ServerCLIOperation(args).execute();
        } else if (line.hasOption("guard")) {
            new GuardCLIOperation(args).execute();
        } else {
            System.out.println("not valid option found: " + Arrays.toString(args));
        }
    }

}
