package io.metadew.iesi.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.definition.generation.GenerationRuleType;
import io.metadew.iesi.metadata.operation.DataObjectOperation;

import java.io.File;
import java.util.HashMap;

public class FrameworkGenerationRuleTypeConfiguration {

	private HashMap<String, GenerationRuleType> generationRuleTypeMap;

	public FrameworkGenerationRuleTypeConfiguration(FrameworkFolderConfiguration frameworkFolderConfiguration) {
		this.initalizeValues(frameworkFolderConfiguration);
	}

	private void initalizeValues(FrameworkFolderConfiguration frameworkFolderConfiguration) {
		this.setGenerationRuleTypeMap(new HashMap<String, GenerationRuleType>());

		StringBuilder initFilePath = new StringBuilder();
		initFilePath.append(frameworkFolderConfiguration.getFolderAbsolutePath("metadata.conf"));
		initFilePath.append(File.separator);
		initFilePath.append("GenerationRuleTypes.json");

		DataObjectOperation dataObjectOperation = new DataObjectOperation();
		dataObjectOperation.setInputFile(initFilePath.toString());
		dataObjectOperation.parseFile();
		ObjectMapper objectMapper = new ObjectMapper();
		for (DataObject dataObject : dataObjectOperation.getDataObjects()) {
			if (dataObject.getType().equalsIgnoreCase("generationruletype")) {
				GenerationRuleType generationRuleType = objectMapper.convertValue(dataObject.getData(), GenerationRuleType.class);
				this.getGenerationRuleTypeMap().put(generationRuleType.getName().toLowerCase(), generationRuleType);
			}
		}
	}

	// Create Getters and Setters
	public GenerationRuleType getGenerationRuleType(String key) {
		return this.getGenerationRuleTypeMap().get(key.toLowerCase());
	}

	public String getGenerationRuleTypeClass(String key) {
		return this.getGenerationRuleTypeMap().get(key.toLowerCase()).getClassName();
	}
	
	public HashMap<String, GenerationRuleType> getGenerationRuleTypeMap() {
		return generationRuleTypeMap;
	}

	public void setGenerationRuleTypeMap(HashMap<String, GenerationRuleType> generationRuleTypeMap) {
		this.generationRuleTypeMap = generationRuleTypeMap;
	}

}