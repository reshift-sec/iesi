package io.metadew.iesi.metadata.repository.service;

import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.definition.MetadataTable;
import io.metadew.iesi.metadata.repository.MetadataRepositorySaveException;

public abstract class MetadataRepositoryService {

    public abstract void createTable(MetadataTable metadataTable);

    public abstract void createAllTables();

    public abstract String generateDDL();

    public abstract String getTableNameByLabel(String label);

    public abstract void save(DataObject dataObject) throws MetadataRepositorySaveException;


}
