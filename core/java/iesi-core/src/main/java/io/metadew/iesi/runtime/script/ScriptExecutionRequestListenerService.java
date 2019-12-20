//package io.metadew.iesi.runtime.script;
//
//import io.metadew.iesi.common.config.ConfigFile;
//import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//public class ScriptExecutionRequestListenerService {
//
//    private static ScriptExecutionRequestListenerService INSTANCE;
//
//    public synchronized static ScriptExecutionRequestListenerService getInstance() {
//        if (INSTANCE == null) {
//            INSTANCE = new ScriptExecutionRequestListenerService();
//        }
//        return INSTANCE;
//    }
//
//    private ScriptExecutionRequestListenerService() {}
//
//
//    private static final Pattern NODE_NUMBER_PATTERN = Pattern.compile("execution_node\\.(?<nodenumber>\\d+)\\..*");
//
//    public void fromConfigFile(ConfigFile configFile) {
//        List<ScriptExecutor> scriptExecutors = new ArrayList<>();
//        List<Integer> nodeNumbers = configFile.getProperties().keySet().stream()
//                .filter(key -> NODE_NUMBER_PATTERN.matcher(key.toString()).find())
//                .map(key -> {
//                    Matcher matcher = NODE_NUMBER_PATTERN.matcher(key.toString());
//                    matcher.find();
//                    return Integer.parseInt(matcher.group("nodenumber"));
//                })
//                .distinct()
//                .collect(Collectors.toList());
//        for (Integer nodeNumber : nodeNumbers) {
//            Pattern NODE_NUMBER_PROPERTIES_PATTERN = Pattern.compile("execution_node\\."+nodeNumber.toString()+"\\.(?<properties>.*)");
//            Map<String, String> nodeProperties = configFile.getProperties().entrySet().stream()
//                    .filter(entry -> NODE_NUMBER_PROPERTIES_PATTERN.matcher(entry.getKey().toString()).find())
//                    .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));
//            scriptExecutors.add(ScriptExecutorService.getInstance().fromProperties(nodeProperties));
//        }
//        ScriptExecutionRequestListener.getInstance();
//
//    }
//
//    public Optional<String> getSettingValue(ConfigFile configFile, String settingPath) {
//        return FrameworkSettingConfiguration.getInstance().getSettingPath(settingPath).flatMap(configFile::getProperty);
//    }
//}
