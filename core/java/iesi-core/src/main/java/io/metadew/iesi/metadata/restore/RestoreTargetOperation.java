/*
package io.metadew.iesi.metadata.restore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.data.definition.DataTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class RestoreTargetOperation {

    private Long processId;
    private String dataFileLocation;
    private static final Logger LOGGER = LogManager.getLogger();

    // Constructors
    public RestoreTargetOperation() {
    }

    // Methods
    public void execute(String dataFile) {
        LOGGER.info("restore.file=" + dataFile);

        try {
            // Parse input file
            File file = new File(dataFile);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                DataObject dataObject = objectMapper.readValue(file, new TypeReference<DataObject>() {
                });
                if (dataObject.getType().equalsIgnoreCase("datatable")) {
                    DataTable dataTable = objectMapper.convertValue(dataObject.getData(), DataTable.class);
                    RestoreTableOperation restoreTableOperation = new RestoreTableOperation(
                            dataTable);
                    restoreTableOperation.execute();

                } else {
                    LOGGER.error("restore.error.object.type.invalid" + dataFile);
                }

            } catch (Exception e) {
                LOGGER.error("restore.error.file.read" + dataFile);
            }
        } catch (Exception e) {
            LOGGER.error("restore.error.file.parse" + dataFile);
        } finally {
            // Log End
            // this.getEoControl().endExecution(this);
        }

    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public String getDataFileLocation() {
        return dataFileLocation;
    }

    public void setDataFileLocation(String dataFileLocation) {
        this.dataFileLocation = dataFileLocation;
    }
}*/
