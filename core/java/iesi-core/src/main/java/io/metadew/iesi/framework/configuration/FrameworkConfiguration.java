package io.metadew.iesi.framework.configuration;

import io.metadew.iesi.common.config.KeyValueConfigFile;
import io.metadew.iesi.connection.tools.FileTools;
import org.apache.logging.log4j.ThreadContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FrameworkConfiguration {

	private String frameworkCode;
	private Path frameworkHome;

	private FrameworkGenerationRuleTypeConfiguration generationRuleTypeConfiguration;

	private static FrameworkConfiguration INSTANCE;

	public synchronized static FrameworkConfiguration getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FrameworkConfiguration();
		}
		return INSTANCE;
	}

	private FrameworkConfiguration() {}


	public void init() {
		ThreadContext.put("fwk.code", FrameworkSettings.IDENTIFIER.value());
		String configurationFile = FrameworkSettings.IDENTIFIER.value() + "-home.conf";
		String frameworkHome;
		if (System.getProperty(FrameworkSettings.IDENTIFIER.value() + ".home") != null) {
			frameworkHome = System.getProperty(FrameworkSettings.IDENTIFIER.value()  + ".home");
		} else if (getClass().getResource(FrameworkSettings.IDENTIFIER.value() + ".home") != null) {
			frameworkHome = getClass().getResource(FrameworkSettings.IDENTIFIER.value()  + ".home").getFile();
		} else if (getClass().getResource(FrameworkSettings.IDENTIFIER.value() + "-home.conf") != null) {
			KeyValueConfigFile home = new KeyValueConfigFile(getClass().getResource(FrameworkSettings.IDENTIFIER.value() + "-home.conf").getFile());
			frameworkHome = home.getProperties().getProperty(FrameworkSettings.IDENTIFIER.value()  + ".home");
		} else if (FileTools.exists(configurationFile)) {
			KeyValueConfigFile home = new KeyValueConfigFile(configurationFile);
			frameworkHome = home.getProperties().getProperty(FrameworkSettings.IDENTIFIER.value()  + ".home");
		} else {
			Path path = Paths.get(".").toAbsolutePath();
			throw new RuntimeException(FrameworkSettings.IDENTIFIER.value()  + ".home not found as System property or " + FrameworkSettings.IDENTIFIER.value() + "-home.conf not found at " + path.getRoot() + " or on classpath");
		}
		init(Paths.get(frameworkHome));
	}

	public void init(Path frameworkHome) {
		this.frameworkCode = FrameworkSettings.IDENTIFIER.value();
		ThreadContext.put("fwk.code", FrameworkSettings.IDENTIFIER.value());
		this.frameworkHome = frameworkHome;

		FrameworkFolderConfiguration folderConfiguration = FrameworkFolderConfiguration.getInstance();
		folderConfiguration.init(frameworkHome);

		FrameworkSettingConfiguration settingConfiguration = FrameworkSettingConfiguration.getInstance();
		settingConfiguration.init(frameworkHome);

		FrameworkActionTypeConfiguration actionTypeConfiguration = FrameworkActionTypeConfiguration.getInstance();
		actionTypeConfiguration.init(folderConfiguration);

		this.generationRuleTypeConfiguration = new FrameworkGenerationRuleTypeConfiguration();

	}

	public void initAssembly(String repositoryHome) {
		// TODO: add core substring in assembly context in order to start the framework with custom iesi home
		//  for testing purposes
		this.frameworkCode = FrameworkSettings.IDENTIFIER.value();
		ThreadContext.put("fwk.code", frameworkCode);
		this.frameworkHome = Paths.get(repositoryHome).resolve("core");

		FrameworkFolderConfiguration folderConfiguration = FrameworkFolderConfiguration.getInstance();
		folderConfiguration.init(frameworkHome);

		FrameworkSettingConfiguration settingConfiguration = FrameworkSettingConfiguration.getInstance();
		settingConfiguration.init(frameworkHome);
	}

	public void init(Path frameworkHome, FrameworkGenerationRuleTypeConfiguration frameworkGenerationRuleTypeConfiguration) {
		this.frameworkCode = FrameworkSettings.IDENTIFIER.value();
		ThreadContext.put("fwk.code", FrameworkSettings.IDENTIFIER.value());
		this.frameworkHome = frameworkHome;

		FrameworkFolderConfiguration folderConfiguration = FrameworkFolderConfiguration.getInstance();
		folderConfiguration.init(frameworkHome);

		FrameworkSettingConfiguration settingConfiguration = FrameworkSettingConfiguration.getInstance();
		settingConfiguration.init(frameworkHome);

		FrameworkActionTypeConfiguration actionTypeConfiguration = FrameworkActionTypeConfiguration.getInstance();
		actionTypeConfiguration.init(folderConfiguration);

		this.generationRuleTypeConfiguration = frameworkGenerationRuleTypeConfiguration;
	}

	public Path getFrameworkHome() {
		return frameworkHome;
	}

	public String getFrameworkCode() {
		return frameworkCode;
	}

	public FrameworkGenerationRuleTypeConfiguration getGenerationRuleTypeConfiguration() {
		return generationRuleTypeConfiguration;
	}

}