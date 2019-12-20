package io.metadew.iesi.launch.server;

import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.launch.Command;
import io.metadew.iesi.runtime.ExecutionRequestListener;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "server", mixinStandardHelpOptions = true, version = "0.2.0")
public class ServerCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Command command;

    private final Object syncObject = new Object();

    public static void main(String[] args) {
        System.exit(new CommandLine(new ServerCommand()).execute(args));
    }

    @Override
    public Integer call() {
        try {
            System.out.println("server.launcher.start");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            Command.initFrameworkInstance(command.ini);
            ExecutionRequestListener executionRequestListener = new ExecutionRequestListener();
            final Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("test");
                synchronized(syncObject) {
                    syncObject.notify();
                }
                try {
                    mainThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
            new Thread(executionRequestListener).start();
            synchronized (syncObject) {
                syncObject.wait();
            }
            executionRequestListener.shutdown();
            FrameworkInstance.getInstance().shutdown();
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("server.launcher.end");
            return 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }

}
