package io.metadew.iesi.metadata.configuration.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.metadata.definition.action.type.ActionType;
import io.metadew.iesi.metadata.operation.DataObjectOperation;
import io.metadew.iesi.metadata.operation.TypeConfigurationOperation;

import java.nio.file.Path;

public class ActionTypeConfiguration {

    private String dataObjectType = "ActionType";

    public ActionTypeConfiguration() {
    }

    public ActionType getActionType(String actionTypeName) {
        DataObjectOperation dataObjectOperation = new DataObjectOperation(TypeConfigurationOperation.getTypeConfigurationFile(dataObjectType, actionTypeName));
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(dataObjectOperation.getDataObject().getData(), ActionType.class);
    }

    public String getDataObjectType() {
        return dataObjectType;
    }

    public void setDataObjectType(String dataObjectType) {
        this.dataObjectType = dataObjectType;
    }

}