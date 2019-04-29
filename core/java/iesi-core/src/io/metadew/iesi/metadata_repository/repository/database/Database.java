package io.metadew.iesi.metadata_repository.repository.database;

import io.metadew.iesi.connection.database.sql.SqlScriptResult;
import io.metadew.iesi.framework.execution.FrameworkLog;
import io.metadew.iesi.metadata.definition.MetadataField;
import io.metadew.iesi.metadata.definition.MetadataTable;
import io.metadew.iesi.metadata_repository.repository.database.connection.DatabaseConnection;

import javax.sql.rowset.CachedRowSet;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;


public abstract class Database {

    DatabaseConnection databaseConnection;

    public Database(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public abstract String getSystemTimestampExpression();

    public abstract String getAllTablesQuery(String pattern);

    @SuppressWarnings("unused")
	public List<String> getAllTables(String pattern) {
        List<String> tables = new LinkedList<>();
        CachedRowSet crsCleanInventory = executeQuery(getAllTablesQuery(pattern));
        try {
            while (crsCleanInventory.next()) {
                String schemaName = crsCleanInventory.getString("OWNER");
                String tableName = crsCleanInventory.getString("TABLE_NAME");
                tables.add(tableName);
            }
            crsCleanInventory.close();
        } catch (Exception e) {
            StringWriter StackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(StackTrace));
        }
        return tables;
    }

    public void cleanAllTables(String pattern, FrameworkLog frameworkLog) {
        getAllTables(pattern).stream()
                .filter(table -> !(table.endsWith("CFG_MTD_TBL") || table.endsWith("CFG_MTD_FLD")))
                .forEach(table -> cleanTable(table, frameworkLog));
    }

    public void cleanTable(String tableName, FrameworkLog frameworkLog) {
        String query = "delete from " + tableName;
        databaseConnection.executeQuery(query);
    }

    public void dropAllTables(String pattern, FrameworkLog frameworkLog) {
        for (String table : getAllTables(pattern)){
            dropTable(table, frameworkLog);
        }
    }

    public void dropTable(String tableName, FrameworkLog frameworkLog) {
        String query = "drop table " + tableName;
        databaseConnection.executeUpdate(query);
    }

    public void executeUpdate(String query) {
        this.databaseConnection.executeUpdate(query);
    }

    public CachedRowSet executeQuery(String query) {
        return this.databaseConnection.executeQuery(query);
    }

    public SqlScriptResult executeScript(String filename) {
        return this.databaseConnection.executeScript(filename);
    }

    public SqlScriptResult executeScript(InputStream inputStream) {
        return this.databaseConnection.executeScript(inputStream);
    }


    public void createTable(MetadataTable metadataTable, String tableNamePrefix) {
        executeUpdate(getCreateStatement(metadataTable, tableNamePrefix));
    }

    public void dropTable(MetadataTable metadataTable, String tableNamePrefix) {
        executeUpdate(getDropStatement(metadataTable, tableNamePrefix));
    }

    public void cleanTable(MetadataTable metadataTable, String tableNamePrefix) {
        executeUpdate(getCleanStatement(metadataTable, tableNamePrefix));
    }

    String getCleanStatement(MetadataTable metadataTable, String tableNamePrefix) {
        return "delete from " + tableNamePrefix + metadataTable.getName();
    }

    public String getDropStatement(MetadataTable table, String tableNamePrefix) {
        return "drop table " + tableNamePrefix + table.getName();
    }

    public String getCreateStatement(MetadataTable table, String tableNamePrefix) {
        StringBuilder createQuery = new StringBuilder();
        StringBuilder fieldComments = new StringBuilder();

        String tableName = tableNamePrefix + table.getName();

        createQuery.append("CREATE TABLE ").append(tableName).append("\n(\n");
        int counter = 1;
        for (MetadataField field : table.getFields()) {
            if (counter > 1) {
                createQuery.append(",\n");
            }
            createQuery.append("\t").append(field.getName());

            int tabNumber = 1;
            if (field.getName().length() >= 8) {
                tabNumber = (int) (4 - Math.ceil((double) field.getName().length() / 8));
            } else {
                tabNumber = 4;
            }

            for (int tabCount = 1; tabCount <= tabNumber; tabCount++) {
                createQuery.append("\t");
            }

            createQuery.append(toQueryString(field));
            if (addComments() && field.getDescription().isPresent()) {
                fieldComments.append("\nCOMMENT ON COLUMN ").append(tableName).append(".").append(field.getName())
                        .append(" IS '").append(field.getDescription().get()).append("';");
            }
            counter++;
        }

        createQuery.append("\n);\n");
        createQuery.append(createQueryExtras());
        createQuery.append(fieldComments).append("\n\n");

        return createQuery.toString();
    }

    public abstract String createQueryExtras();

    public abstract boolean addComments();

    public abstract String toQueryString(MetadataField field);

}