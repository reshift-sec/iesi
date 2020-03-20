package io.metadew.iesi.connection.operation;

import io.metadew.iesi.connection.database.OracleDatabase;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.metadata.definition.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.metadew.iesi.connection.database.connection.oracle.OracleDatabaseConnection;

import java.text.MessageFormat;

public class DbOracleConnectionService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static DbOracleConnectionService INSTANCE;

    private final static String connectionUrlKey = "connectionURL";
    private final static String schemaKey = "schema";
    private final static String userKey = "user";
    private final static String passwordKey = "password";

    public synchronized static DbOracleConnectionService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DbOracleConnectionService();
        }
        return INSTANCE;
    }

    private DbOracleConnectionService() {
    }

    public OracleDatabase getDatabase(Connection connection)  {

        String connectionUrl = getMandatoryParameterWithKey(connection, connectionUrlKey);
        String schemaName = getMandatoryParameterWithKey(connection, schemaKey);
        String userName = getMandatoryParameterWithKey(connection, userKey);
        String userPassword = getMandatoryParameterWithKey(connection, passwordKey);

        OracleDatabaseConnection oracleDatabaseConnection = new OracleDatabaseConnection(connectionUrl,
                userName,
                userPassword);
        return new OracleDatabase(oracleDatabaseConnection, schemaName);
    }

    private String getMandatoryParameterWithKey(Connection connection, String key) {
        return connection.getParameters().stream()
                .filter(connectionParameter -> connectionParameter.getName().equalsIgnoreCase(key))
                .findFirst()
                .map(connectionParameter -> FrameworkControl.getInstance().resolveConfiguration(connectionParameter.getValue()))
                .map(connectionParameterValue -> FrameworkCrypto.getInstance().decryptIfNeeded(connectionParameterValue))
                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Connection {0} does not contain mandatory parameter ''{1}''", connection, key)));
    }
}
