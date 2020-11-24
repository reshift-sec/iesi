package io.metadew.iesi.datatypes.dataset.metadata;

import io.metadew.iesi.common.configuration.framework.FrameworkConfiguration;
import io.metadew.iesi.connection.database.Database;
import io.metadew.iesi.connection.database.DatabaseHandler;
import io.metadew.iesi.connection.database.sqlite.SqliteDatabase;
import io.metadew.iesi.connection.database.sqlite.SqliteDatabaseConnection;
import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.datatypes.dataset.keyvalue.KeyValueDataset;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class DatasetMetadataService {


    private static DatasetMetadataService INSTANCE;

    public synchronized static DatasetMetadataService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DatasetMetadataService();
        }
        return INSTANCE;
    }

    private DatasetMetadataService() {
    }

    public DatasetMetadata getByName(String datasetName) {
        return new DatasetMetadata(datasetName, new SqliteDatabase(new SqliteDatabaseConnection(
                FrameworkConfiguration.getInstance()
                        .getMandatoryFrameworkFolder("data")
                        .getAbsolutePath()
                        .resolve("datasets")
                        .resolve(datasetName)
                        .resolve("metadata")
                        .resolve("metadata.db3"),
                null)));
    }


    public Optional<Long> getId(DatasetMetadata datasetMetadata, List<String> labels) {
        try {
            String getByLabelSetValueSubQuery = "SELECT " +
                    "dataset_impl_labels.DATASET_INV_ID " +
                    "FROM CFG_DATASET_LBL dataset_impl_labels " +
                    "WHERE dataset_impl_labels.DATASET_LBL_VAL = {0}";

            String getByLabelSetCountSubQuery = "SELECT " +
                    "dataset_impl_labels.DATASET_INV_ID " +
                    "FROM CFG_DATASET_LBL dataset_impl_labels " +
                    "GROUP BY dataset_impl_labels.DATASET_INV_ID " +
                    "HAVING COUNT(DISTINCT dataset_impl_labels.DATASET_LBL_VAL) = {0}";

            String labelSetQuery = labels.stream()
                    .map(s -> MessageFormat.format(getByLabelSetValueSubQuery, SQLTools.GetStringForSQL(s)))
                    .collect(Collectors.joining(" intersect "));
            labelSetQuery = labelSetQuery + " intersect " + MessageFormat.format(getByLabelSetCountSubQuery, SQLTools.GetStringForSQL(labels.size()));



//            String query = "SELECT DATASET_INV_ID FROM CFG_DATASET_LBL WHERE DATASET_LBL_VAL in (" + labels.stream().map(SQLTools::GetStringForSQL).collect(Collectors.joining(",")) +
//                    ") GROUP BY DATASET_INV_ID HAVING COUNT(DISTINCT DATASET_LBL_VAL) = " + labels.size() + ";";
            CachedRowSet cachedRowSet = DatabaseHandler.getInstance().executeQuery(datasetMetadata.getDatabase(), labelSetQuery);
            if (cachedRowSet.size() == 0) {
                return Optional.empty();
            } else if (cachedRowSet.size() > 1) {
                log.trace(MessageFormat.format("Found multiple dataset ids ({0}) for labels {1}-{2}. Returning first occurence",
                        cachedRowSet.size(), datasetMetadata.getDatasetName(), String.join(", ", labels)));
            }
            List<Long> ids = new ArrayList<>();
            while (cachedRowSet.next()) {
                ids.add(cachedRowSet.getLong("DATASET_INV_ID"));
            }
            if (ids.size() > 1) {
                log.warn(MessageFormat.format("Found multiple dataset id ({0}) linked to labels {1}-{2}.", ids.stream().map(Object::toString).collect(Collectors.joining(", ")), datasetMetadata.getDatasetName(), String.join(", ", labels)));
            }
            return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(ids.size() - 1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Database getDatasetDatabase(DatasetMetadata datasetMetadata, long id) {
        try {
            String query = "select DATASET_FILE_NM, DATASET_TABLE_NM from CFG_DATASET_INV where DATASET_INV_ID = " + id;
            CachedRowSet cachedRowSetFileTable = DatabaseHandler.getInstance().executeQuery(datasetMetadata.getDatabase(), query);


            if (cachedRowSetFileTable.size() == 0) {
                throw new RuntimeException(MessageFormat.format("dataset id {0} is does not have an implementation. " +
                        "Please implement this dataset", datasetMetadata.getDatasetName()));
            } else if (cachedRowSetFileTable.size() > 1) {
                log.warn(MessageFormat.format("Found more than implementation for dataset id {0}. " +
                        "Returning first occurrence.", id));
            }
            cachedRowSetFileTable.next();
            Database database = new SqliteDatabase(new SqliteDatabaseConnection(FrameworkConfiguration.getInstance()
                    .getMandatoryFrameworkFolder("data")
                    .getAbsolutePath().resolve("datasets")
                    .resolve(datasetMetadata.getDatasetName())
                    .resolve("data")
                    .resolve(cachedRowSetFileTable.getString("DATASET_FILE_NM")),
                    null));
            cachedRowSetFileTable.close();
            return database;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTableName(DatasetMetadata datasetMetadata, long id) {
        try {
            String query = "select DATASET_FILE_NM, DATASET_TABLE_NM from CFG_DATASET_INV where DATASET_INV_ID = " + id;
            CachedRowSet cachedRowSetFileTable = DatabaseHandler.getInstance().executeQuery(datasetMetadata.getDatabase(), query);


            if (cachedRowSetFileTable.size() == 0) {
                throw new RuntimeException(MessageFormat.format("dataset id {0} is does not have an implementation. " +
                        "Please implement this dataset", datasetMetadata.getDatasetName()));
            } else if (cachedRowSetFileTable.size() > 1) {

                log.warn(MessageFormat.format("Found more than implementation for dataset id {0}. " +
                        "Returning first occurrence.", id));

            }
            cachedRowSetFileTable.next();
            String tableName = cachedRowSetFileTable.getString("DATASET_TABLE_NM");
            cachedRowSetFileTable.close();
            return tableName;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertDatasetLabelInformation(DatasetMetadata datasetMetadata, int datasetInventoryId, List<String> labels) {
        String labelQuery = "insert into CFG_DATASET_LBL (DATASET_INV_ID, DATASET_LBL_VAL) VALUES ("
                + SQLTools.GetStringForSQL(datasetInventoryId) + ", {0})";
        labels.forEach(label -> DatabaseHandler.getInstance().executeUpdate(datasetMetadata.getDatabase(), MessageFormat.format(labelQuery, SQLTools.GetStringForSQL(label))));
    }

    @Synchronized
    public int getLatestInventoryId(DatasetMetadata datasetMetadata) {
        try {
            String latestInventoryIdQuery = "select max(DATASET_INV_ID) as LATEST_INVENTORY_ID from (SELECT DATASET_INV_ID FROM CFG_DATASET_INV " +
                    "UNION ALL SELECT DATASET_INV_ID FROM CFG_DATASET_LBL);";
            CachedRowSet cachedRowSet = DatabaseHandler.getInstance().executeQuery(datasetMetadata.getDatabase(), latestInventoryIdQuery);
            int inventoryId;
            if (cachedRowSet.size() == 0) {
                inventoryId = 0;
            } else {
                cachedRowSet.next();
                inventoryId = cachedRowSet.getInt("LATEST_INVENTORY_ID");
            }
            cachedRowSet.close();
            return inventoryId;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertDatasetDatabaseInformation(DatasetMetadata datasetMetadata, int inventoryId, String filename, String tableName) {
        String inventoryQuery = "insert into CFG_DATASET_INV (DATASET_INV_ID, DATASET_FILE_NM, DATASET_TABLE_NM)" +
                " Values (" + inventoryId + ", \"" + filename + "\", \"" + tableName + "\")";
        DatabaseHandler.getInstance().executeUpdate(datasetMetadata.getDatabase(), inventoryQuery);
    }

    public void shutdown(DatasetMetadata datasetMetadata) {
        DatabaseHandler.getInstance().shutdown(datasetMetadata.getDatabase());
    }

    public void delete(DatasetMetadata datasetMetadata, KeyValueDataset keyValueDataset) {
        Optional<Long> id = getId(datasetMetadata, keyValueDataset.getLabels());
        while (id.isPresent()) {
            log.warn(MessageFormat.format("deleting dataset with id {0}", id.get()));
            String deleteQuery = MessageFormat.format("DELETE FROM CFG_DATASET_LBL where DATASET_INV_ID={0}", id.get());
            DatabaseHandler.getInstance().executeUpdate(datasetMetadata.getDatabase(), deleteQuery);
            id = getId(datasetMetadata, keyValueDataset.getLabels());
        }
    }

}
