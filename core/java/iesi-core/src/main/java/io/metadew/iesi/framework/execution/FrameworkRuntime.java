package io.metadew.iesi.framework.execution;

import io.metadew.iesi.common.properties.PropertiesTools;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import io.metadew.iesi.framework.definition.FrameworkRunIdentifier;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.ThreadContext;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

public class FrameworkRuntime {

	private String runCacheFolderName;
	private String localHostChallenge;
	private String localHostChallengeFileName;
	private String runSpoolFolderName;
	private String processIdFileName;
	private String frameworkRunId;

	private static FrameworkRuntime INSTANCE;

	public synchronized static FrameworkRuntime getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FrameworkRuntime();
		}
		return INSTANCE;
	}

	private FrameworkRuntime() {}

	public void init() {
		init(new FrameworkRunIdentifier());
	}

	public void init(FrameworkRunIdentifier frameworkRunIdentifier) {
		init(frameworkRunIdentifier.getId());
	}

	public void init(String runId) {
		this.frameworkRunId = runId;
		ThreadContext.put("fwk.runid", runId);
		this.runCacheFolderName = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("run.cache")
				+ File.separator + this.frameworkRunId;
		FolderTools.createFolder(runCacheFolderName);

		this.runSpoolFolderName = this.runCacheFolderName + File.separator + "spool";
		FolderTools.createFolder(runSpoolFolderName);

		this.localHostChallenge = UUID.randomUUID().toString();
		this.localHostChallengeFileName = FilenameUtils.normalize(runCacheFolderName + File.separator + this.localHostChallenge  + ".fwk");
		FileTools.appendToFile(localHostChallengeFileName, "", "localhost.challenge=" + this.localHostChallenge);

		// Initialize process id
		this.processIdFileName = FilenameUtils.normalize(runCacheFolderName + File.separator  + "processId.fwk");
		Properties processIdProperties = new Properties();
		processIdProperties.put("processId", "-1");
		PropertiesTools.setProperties(processIdFileName, processIdProperties);
	}


	public void terminate() {
		FolderTools.deleteFolder(runCacheFolderName, true);
	}


	public String getFrameworkRunId() {
		return frameworkRunId;
	}

	public String getLocalHostChallengeFileName() {
		return localHostChallengeFileName;
	}

}

