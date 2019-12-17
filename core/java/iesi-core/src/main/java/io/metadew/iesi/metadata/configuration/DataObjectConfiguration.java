package io.metadew.iesi.metadata.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.framework.configuration.FrameworkObjectConfiguration;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import io.metadew.iesi.metadata.repository.MetadataRepositorySaveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

public class DataObjectConfiguration {

    private List<DataObject> dataObjects;
    private static final Logger LOGGER = LogManager.getLogger();

    public DataObjectConfiguration(List<DataObject> dataObjects) {
        this.dataObjects = dataObjects;
    }

    // Methods
    public DataObject getDataObject(Object object) {
        String type = FrameworkObjectConfiguration.getFrameworkObjectType(object);
        return new DataObject(type, object);
    }

    public DataObject getDataObject(String data) {
        DataObject dataObject = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            dataObject = objectMapper.readValue(data, new TypeReference<DataObject>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataObject;
    }

    public void saveToMetadataRepository(MetadataRepository metadataRepository) {
        for (DataObject dataObject : dataObjects) {
            try {
                metadataRepository.save(dataObject);
            } catch (MetadataRepositorySaveException e) {
                LOGGER.warn(MessageFormat.format("Failed to save {0} to repository", dataObject.getType()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<DataObject> getDataObjects() {
        return dataObjects;
    }

}