package io.metadew.iesi.metadata.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Files;
import io.metadew.iesi.metadata.configuration.DataObjectConfiguration;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataObjectOperation {

	private static final PathMatcher JSON_MATCHER = FileSystems.getDefault().getPathMatcher("regex:.+\\.json");
	private static final PathMatcher YML_MATCHER = FileSystems.getDefault().getPathMatcher("regex:.+\\.yml");

	private Path inputFile;
	private List<DataObject> dataObjects;
	private DataObject dataObject;
	private DataObjectConfiguration dataObjectConfiguration;

	// Constructors
	public DataObjectOperation() {
	}

	public DataObjectOperation(String inputFile) {
		this(Paths.get(inputFile));
	}

	public DataObjectOperation(Path inputFile) {
		this.inputFile = inputFile;
		if (JSON_MATCHER.matches(inputFile)) {
			parseFile();
		} else if (YML_MATCHER.matches(inputFile)) {
			parseYamlFile();
		} else {
			throw new RuntimeException("Data object is neither defined in json or yaml format: " + inputFile.toString());
		}
	}


	// Methods
	private void parseFile() {
		// Define input file
		File file = inputFile.toFile();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			String readLine = "";
			boolean jsonArray = true;

			while ((readLine = bufferedReader.readLine()) != null) {
				if (readLine.trim().toLowerCase().startsWith("[") && (!readLine.trim().equalsIgnoreCase(""))) {
					jsonArray = true;
					break;
				} else if (!readLine.trim().equalsIgnoreCase("")) {
					jsonArray = false;
					break;
				}
			}

			ObjectMapper objectMapper = new ObjectMapper();
			if (jsonArray) {
				dataObjects = objectMapper.readValue(file, new TypeReference<List<DataObject>>() {});
			} else {
				dataObject = objectMapper.readValue(file, new TypeReference<DataObject>() {});
				dataObjects = new ArrayList<>();
				dataObjects.add(dataObject);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void parseYamlFile() {
		// Define input file
		File file = inputFile.toFile();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			String readLine = "";
			boolean yamlArray = true;
			int i = 0;
			while ((readLine = bufferedReader.readLine()) != null) {
				if (readLine.trim().toLowerCase().startsWith("---") && (!readLine.trim().equalsIgnoreCase(""))) {
					// TODO add support for multiple documents in file

					i++;
					continue;
				} else if (i == 1) {
					if (readLine.trim().toLowerCase().startsWith("-") && (!readLine.trim().equalsIgnoreCase(""))) {
						yamlArray = true;
						break;
					} else if (!readLine.trim().equalsIgnoreCase("")) {
						yamlArray = false;
						break;
					}
				} else if (!readLine.trim().equalsIgnoreCase("")) {
					yamlArray = false;
					break;
				}
			}
			bufferedReader.close();

			dataObjects = new ArrayList<>();
			ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
			if (yamlArray) {
				// dataObjects = objectMapper.readValue(file, new
				// TypeReference<List<DataObject>>() { }));
				
				// Work around for reading arrays immediate if from start
				bufferedReader = new BufferedReader(new FileReader(file));
				readLine = "";
				StringBuilder dataObjectRead = null;
				
				while ((readLine = bufferedReader.readLine()) != null) {
					if (readLine.trim().toLowerCase().startsWith("---") && (!readLine.trim().equalsIgnoreCase(""))) {
						continue;
					} else if (readLine.trim().toLowerCase().startsWith("-") && (!readLine.trim().equalsIgnoreCase(""))) {
						if (dataObjectRead != null) {
							DataObject dataObject = objectMapper.readValue(dataObjectRead.toString(), new TypeReference<DataObject>() {
							});
							dataObjects.add(dataObject);
						}
						dataObjectRead = new StringBuilder();
						dataObjectRead.append("---");
						dataObjectRead.append("\n");
						dataObjectRead.append(readLine.replace("- ", ""));
					} else {
						dataObjectRead.append("\n");
						dataObjectRead.append(readLine.substring(2));
					}
				}

			} else {
				dataObject = objectMapper.readValue(file, new TypeReference<DataObject>() {});
				dataObjects = new ArrayList<>();
				dataObjects.add(dataObject);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// TODO remove from this operation - create new one
	public void saveToMetadataRepository(List<MetadataRepository> metadataRepositories) {
		for (MetadataRepository metadataRepository : metadataRepositories) {
			this.setDataObjectConfiguration(new DataObjectConfiguration(dataObjects));
			this.getDataObjectConfiguration().saveToMetadataRepository(metadataRepository);
		}
	}

	public DataObjectConfiguration getDataObjectConfiguration() {
		return dataObjectConfiguration;
	}

	public void setDataObjectConfiguration(DataObjectConfiguration dataObjectConfiguration) {
		this.dataObjectConfiguration = dataObjectConfiguration;
	}

	public List<DataObject> getDataObjects() {
		return dataObjects;
	}

	public DataObject getDataObject() {
		return dataObject;
	}

}