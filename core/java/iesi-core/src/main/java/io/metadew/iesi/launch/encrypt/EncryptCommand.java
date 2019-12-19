package io.metadew.iesi.launch.encrypt;

import io.metadew.iesi.framework.configuration.FrameworkConfiguration;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.launch.Command;
import picocli.CommandLine;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.Console;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "encrypt", mixinStandardHelpOptions = true, version = "0.2.0")
public class EncryptCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Command command;


    public static void main(String[] args) {
        System.exit(new CommandLine(new EncryptCommand()).execute(args));
    }

    @Override
    public Integer call() {
        try {
            Command.initFrameworkInstance(command.ini);

            Console console = System.console();
            if (console == null) {
                System.out.println("Couldn't get Console instance");
                System.exit(1);
            }

            char[] passwordArray = console.readPassword("Enter the password to encrypt: ");

            String input = new String(passwordArray);

            FrameworkConfiguration.getInstance().init();
            FrameworkCrypto frameworkCrypto = FrameworkCrypto.getInstance();
            String output = frameworkCrypto.encrypt(input);

            System.out.println("The encrypted password is: " + output);

            StringSelection stringSelection = new StringSelection(output);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);

            System.out.println("The encrypted password has been copied to the clipboard");
            return 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }

}
