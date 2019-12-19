package io.metadew.iesi.test.launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.Permission;
import java.util.List;

public final class Launcher {

    private final static Logger LOGGER = LogManager.getLogger();

    public static void execute(List<LaunchArgument> inputArgs) throws Exception {
        LaunchArgument option = null;

        int inputArgsArraySize = 0;
        for (LaunchArgument launchArgument : inputArgs) {
            if (launchArgument.isKeyvalue()) {
                inputArgsArraySize = inputArgsArraySize + 2;
            } else {
                inputArgsArraySize++;
            }
        }

        String[] inputArgsArray = new String[inputArgsArraySize];
        int k = 0;
        int i = 0;
        while (i < inputArgsArraySize) {
            LaunchArgument launchArgument = inputArgs.get(k);
            if (launchArgument.isKeyvalue()) {
                inputArgsArray[i] = launchArgument.getKey();
                inputArgsArray[i + 1] = launchArgument.getValue();
                i = i + 2;
            } else {
                inputArgsArray[i] = launchArgument.getKey();
                i++;
            }
            k++;
        }
        inputArgs.remove(option);
		SecurityManager securityManager = System.getSecurityManager();
		try {
            System.setSecurityManager(new MySecurityManager());
            io.metadew.iesi.launch.Command.main(inputArgsArray);
        } catch (SecurityException e) {
            LOGGER.trace("prevented application from system exit");
        } finally {
			System.setSecurityManager(securityManager);
		}
    }

    static class MySecurityManager extends SecurityManager {
        @Override
        public void checkExit(int status) {
            throw new SecurityException();
        }


        @Override
        public void checkPermission(Permission perm) {
            // Allow other activities by default
        }

    }

}