package io.metadew.iesi.script.execution;

import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.metadata.execution.MetadataControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RootStrategy implements RootingStrategy {


    private static final Logger LOGGER = LogManager.getLogger();

    public RootStrategy() {}

    @Override
    public void prepareExecution(ScriptExecution scriptExecution) {
        scriptExecution.getExecutionControl().getExecutionRuntime().setRuntimeVariablesFromList(scriptExecution, MetadataControl.getInstance()
                .getConnectivityMetadataRepository()
                .executeQuery("select env_par_nm, env_par_val from "
                        + MetadataControl.getInstance().getConnectivityMetadataRepository().getTableNameByLabel("EnvironmentParameters")
                        + " where env_nm = " + SQLTools.GetStringForSQL(scriptExecution.getEnvironment()) + " order by env_par_nm asc, env_par_val asc", "reader"));
    }

    @Override
    public void endExecution(ScriptExecution scriptExecution) {
        scriptExecution.getExecutionControl().terminate();
        if (scriptExecution.isExitOnCompletion()) {
            // scriptExecution.getExecutionControl().endExecution();
        }
        scriptExecution.getExecutionControl().getExecutionRuntime().getRuntimeVariableConfiguration().shutdown();
    }

}
