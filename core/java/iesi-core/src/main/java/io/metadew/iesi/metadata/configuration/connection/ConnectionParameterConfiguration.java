package io.metadew.iesi.metadata.configuration.connection;

import io.metadew.iesi.common.configuration.metadata.repository.MetadataRepositoryConfiguration;
import io.metadew.iesi.common.configuration.metadata.tables.MetadataTablesConfiguration;
import io.metadew.iesi.connection.database.Database;
import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.metadata.configuration.Configuration;
import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.definition.connection.ConnectionParameter;
import io.metadew.iesi.metadata.definition.connection.key.ConnectionKey;
import io.metadew.iesi.metadata.definition.connection.key.ConnectionParameterKey;
import io.metadew.iesi.metadata.repository.MetadataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.rowset.CachedRowSet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConnectionParameterConfiguration extends Configuration<ConnectionParameter, ConnectionParameterKey> {

    private static ConnectionParameterConfiguration INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger();

    public synchronized static ConnectionParameterConfiguration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConnectionParameterConfiguration();
        }
        return INSTANCE;
    }

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ConnectionParameterConfiguration() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(MetadataRepositoryConfiguration.getInstance()
                .getDesignMetadataRepository()
                .getRepositoryCoordinator()
                .getDatabases().values().stream()
                .findFirst()
                .map(Database::getConnectionPool)
                .orElseThrow(RuntimeException::new));
    }

    public void init(MetadataRepository metadataRepository) {
        setMetadataRepository(metadataRepository);
    }

    private static final String query = "select CONN_NM, CONN_PAR_NM, CONN_PAR_VAL, ENV_NM from " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ConnectionParameters").getName()
            + " WHERE CONN_NM= :name AND ENV_NM  = :environment AND CONN_PAR_NM = :parameterName;";
    private static final String queryAll = "select * from  " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ConnectionParameters").getName()
            + " order by CONN_NM ASC;";
    private static final String deleteStatement = "DELETE FROM  " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ConnectionParameters").getName()
            + " WHERE CONN_NM= :name AND ENV_NM  = :environment AND CONN_PAR_NM = :parameterName;";
    private static final String insert = " INSERT INTO " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ConnectionParameters").getName()
            + " (CONN_NM, ENV_NM, CONN_PAR_NM, CONN_PAR_VAL) VALUES ( :name, :environment, :parameterName, :value)";
    private static final String getByConnection = "select CONN_NM, CONN_PAR_NM, CONN_PAR_VAL, ENV_NM from " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ConnectionParameters").getName()
            + " WHERE CONN_NM= :name AND ENV_NM  = :environment order by CONN_NM ASC;";
    private static final String update = "UPDATE " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ConnectionParameters").getName()
            + " SET CONN_PAR_VAL = :value WHERE CONN_NM= :name AND ENV_NM  = :environment AND CONN_PAR_NM = :parameterName;";
    private static final String deleteByConnection = "DELETE FROM  " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ConnectionParameters").getName()
            + " WHERE CONN_NM= :name AND ENV_NM  = :environment";

    @Override
    public Optional<ConnectionParameter> get(ConnectionParameterKey connectionParameterKey) {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", connectionParameterKey.getConnectionKey().getName())
                .addValue("environment", connectionParameterKey.getConnectionKey().getEnvironmentKey().getName())
                .addValue("parameterName", connectionParameterKey.getParameterName());
        return Optional.ofNullable(
                DataAccessUtils.singleResult(namedParameterJdbcTemplate.query(
                        query,
                        sqlParameterSource,
                        new ConnectionParameterExtractor())));
    }

    @Override
    public List<ConnectionParameter> getAll() {
        return namedParameterJdbcTemplate.query(queryAll, new ConnectionParameterExtractor());
    }

    @Override
    public void delete(ConnectionParameterKey metadataKey) {
        LOGGER.trace(MessageFormat.format("Deleting Connection {0}.", metadataKey.toString()));
        if (!exists(metadataKey)) {
            throw new MetadataDoesNotExistException(metadataKey);
        }
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", metadataKey.getConnectionKey().getName())
                .addValue("environment", metadataKey.getConnectionKey().getEnvironmentKey().getName())
                .addValue("parameterName", metadataKey.getParameterName());
        namedParameterJdbcTemplate.update(
                deleteStatement,
                sqlParameterSource);
    }

    @Override
    public void insert(ConnectionParameter connectionParameter) {
        LOGGER.trace(MessageFormat.format("Inserting Connection {0}.", connectionParameter.getMetadataKey().toString()));
        if (exists(connectionParameter.getMetadataKey())) {
            throw new MetadataAlreadyExistsException(connectionParameter.getMetadataKey());
        }
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", connectionParameter.getMetadataKey().getConnectionKey().getName())
                .addValue("environment", connectionParameter.getMetadataKey().getConnectionKey().getEnvironmentKey().getName())
                .addValue("parameterName", connectionParameter.getMetadataKey().getParameterName())
                .addValue("value", connectionParameter.getValue());
        namedParameterJdbcTemplate.update(
                insert,
                sqlParameterSource);
    }

    public void deleteByConnection(ConnectionKey connectionKey) {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", connectionKey.getName())
                .addValue("environment", connectionKey.getEnvironmentKey().getName());
        namedParameterJdbcTemplate.update(
                deleteByConnection,
                sqlParameterSource);
    }

    public List<ConnectionParameter> getByConnection(ConnectionKey connectionKey) {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", connectionKey.getName())
                .addValue("environment", connectionKey.getEnvironmentKey().getName());
        List<ConnectionParameter> connectionParameters = namedParameterJdbcTemplate.query(getByConnection, sqlParameterSource, new ConnectionParameterExtractor());
        return connectionParameters;
    }

    public void update(ConnectionParameter connectionParameter) {
        if (exists(connectionParameter)) {
            SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                    .addValue("value", connectionParameter.getValue())
                    .addValue("name", connectionParameter.getMetadataKey().getConnectionKey().getName())
                    .addValue("environment", connectionParameter.getMetadataKey().getConnectionKey().getEnvironmentKey().getName())
                    .addValue("parameterName", connectionParameter.getMetadataKey().getParameterName());
            namedParameterJdbcTemplate.update(
                    update,
                    sqlParameterSource);
        } else {
            insert(connectionParameter);
        }
    }
}