//package io.metadew.iesi.metadata.restore;
//
//import io.metadew.iesi.connection.FileConnection;
//import io.metadew.iesi.connection.tools.FolderTools;
//import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
//import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
//import io.metadew.iesi.framework.instance.FrameworkInstance;
//import io.metadew.iesi.framework.instance.FrameworkInstance;
//import io.metadew.iesi.metadata.definition.Context;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.lang.reflect.InvocationTargetException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class RestoreExecution {
//
//	private Long processId;
//	private static final Logger LOGGER = LogManager.getLogger();
//
//	// Constructors
//	public RestoreExecution() throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
//			IllegalAccessException, InvocationTargetException, SQLException {
//		//TODO:
//		// Create the framework instance
//		FrameworkInstance.getInstance().init(new FrameworkInitializationFile(), new FrameworkExecutionContext(new Context("restore", "")));
//	}
//
//	// Methods
//	@SuppressWarnings("rawtypes")
//	public void execute(String path) {
//		// Log Start
//		// this.getExecutionControl().logStart(this);
//		this.setProcessId(0L);
//
//		// Verify input parameters
//		if (FolderTools.isFolder(path) ) {
//			LOGGER.debug("restore.error.path.isFolder");
//			// Get source configuration
//			@SuppressWarnings("unchecked")
//			List<FileConnection> fileConnectionList = FolderTools.getConnectionsInFolder(path, "all", "", new ArrayList());
//			for (FileConnection fileConnection : fileConnectionList) {
//				RestoreTargetOperation restoreTargetOperation = new RestoreTargetOperation();
//				restoreTargetOperation.execute(fileConnection.getFilePath());
//			}
//		} else {
//			LOGGER.debug("restore.error.path.isFile");
//			RestoreTargetOperation restoreTargetOperation = new RestoreTargetOperation();
//			restoreTargetOperation.execute(path);
//		}
//
//	}
//
//	public Long getProcessId() {
//		return processId;
//	}
//
//	public void setProcessId(Long processId) {
//		this.processId = processId;
//	}
//
//}