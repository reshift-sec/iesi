package io.metadew.iesi.framework.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.definition.generation.GenerationRuleType;
import io.metadew.iesi.metadata.operation.DataObjectOperation;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public class FrameworkGenerationRuleTypeConfiguration {

	private HashMap<String, GenerationRuleType> generationRuleTypeMap;

	public FrameworkGenerationRuleTypeConfiguration() {
		this.setGenerationRuleTypeMap(new HashMap<>());

		Path initFilePath = FrameworkFolderConfiguration.getInstance().getFolderAbsolutePath("metadata.conf").resolve("GenerationRuleTypes.json");
		DataObjectOperation dataObjectOperation = new DataObjectOperation(initFilePath);
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