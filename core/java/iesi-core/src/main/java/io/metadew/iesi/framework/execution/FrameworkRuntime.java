package io.metadew.iesi.framework.execution;

import io.metadew.iesi.common.properties.PropertiesTools;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.framework.configuration.FrameworkFolderConfiguration;
import io.metadew.iesi.framework.definition.FrameworkRunIdentifier;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.ThreadContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.UUID;

public class FrameworkRuntime {

	private Path runCacheFolderName;
	private Path localHostChallengeFileName;
	private String frameworkRunId;

	private static FrameworkRuntime INSTANCE;

	public synchronized static FrameworkRuntime getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FrameworkRuntime();
		}
		return INSTANCE;
	}

	private FrameworkRuntime() {}

	public void init() throws IOException {
		init(new FrameworkRunIdentifier());
	}

	public void init(FrameworkRunIdentifier frameworkRunIdentifier) throws IOException {
		init(frameworkRunIdentifier.getId());
	}

	public void init(String runId) throws IOException {
		this.frameworkRunId = runId;
		ThreadContext.put("fwk.runid", runId);
		this.runCacheFolderName = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("run.cache").resolve(this.frameworkRunId);
		Files.createDirectory(runCacheFolderName);
//
//		this.runSpoolFolderName = this.runCacheFolderName + File.separator + "spool";
//		FolderTools.createFolder(runSpoolFolderName);
//
		String challenge = UUID.randomUUID().toString();
		this.localHostChallengeFileName = runCacheFolderName.resolve(challenge + ".fwk");
		Files.createFile(localHostChallengeFileName);
		Files.write(localHostChallengeFileName, ("localhost.challenge=" + challenge).getBytes(Charset.defaultCharset()));
//
//		// Initialize process id
//		this.processIdFileName = FilenameUtils.normalize(runCacheFolderName + File.separator  + "processId.fwk");
//		Properties processIdProperties = new Properties();
//		processIdProperties.put("processId", "-1");
//		PropertiesTools.setProperties(processIdFileName, processIdProperties);
	}


	public void terminate() {
		try {
			Files.walkFileTree(runCacheFolderName, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			// Files.deleteIfExists(runCacheFolderName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFrameworkRunId() {
		return frameworkRunId;
	}

	public Path getLocalHostChallengeFileName() {
		return localHostChallengeFileName;
	}

}

