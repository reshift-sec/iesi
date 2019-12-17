package io.metadew.iesi.test.launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class Launcher {

	private final static Logger LOGGER = LogManager.getLogger();

    public static void execute(String launcher, List<LaunchArgument> inputArgs) throws Exception {
		LaunchArgument option = null;
        switch (launcher) {
            case "metadata":
                option = new LaunchArgument(false, "-metadata", "");
				inputArgs.add(0, option);
                break;
            case "script":
                option = new LaunchArgument(false, "-script", "");
                inputArgs.add(0, option);
				break;
			default:
				LOGGER.info("unknown type: " + launcher);
		}

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
		io.metadew.iesi.launch.Launcher.main(inputArgsArray);

    }

}