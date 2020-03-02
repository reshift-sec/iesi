package io.metadew.iesi.connection.operation;

import io.metadew.iesi.connection.database.TeradataDatabase;
import io.metadew.iesi.connection.database.connection.TeradataDatabaseConnection;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.metadata.definition.connection.Connection;
import lombok.extern.log4j.Log4j2;

import java.text.MessageFormat;
import java.util.Optional;

@Log4j2
public class DbTeradataConnectionService {

    private final static String hostKey = "host";
    private final static String databaseKey = "database";
    private final static String userKey = "user";
    private final static String passwordKey = "password";

    private static DbTeradataConnectionService INSTANCE;

    public synchronized static DbTeradataConnectionService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DbTeradataConnectionService();
        }
        return INSTANCE;
    }

    private DbTeradataConnectionService() {
    }

    public TeradataDatabase getDatabase(Connection connection) {

        String hostName = getMandatoryParameterWithKey(connection, hostKey);
        String databaseName = getMandatoryParameterWithKey(connection, databaseKey);
        String userName = getMandatoryParameterWithKey(connection, userKey);
        String userPassword = getMandatoryParameterWithKey(connection, passwordKey);

        TeradataDatabaseConnection teradataDatabaseConnection = new TeradataDatabaseConnection(hostName, 0, databaseName, userName, userPassword);
        return new TeradataDatabase(teradataDatabaseConnection);
    }

    private String getMandatoryParameterWithKey(Connection connection, String key) {
        return connection.getParameters().stream()
                .filter(connectionParameter -> connectionParameter.getName().equalsIgnoreCase(key))
                .findFirst()
                .map(connectionParameter -> FrameworkControl.getInstance().resolveConfiguration(connectionParameter.getValue()))
                .map(connectionParameterValue -> FrameworkCrypto.getInstance().decryptIfNeeded(connectionParameterValue))
                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Connection {0} does not contain mandatory parameter ''{1}''", connection, key)));

    }

    private Optional<String> getOptionalParameterWithKey(Connection connection, String key) {
        return connection.getParameters().stream()
                .filter(connectionParameter -> connectionParameter.getName().equalsIgnoreCase(key))
                .findFirst()
                .map(connectionParameter -> FrameworkControl.getInstance().resolveConfiguration(connectionParameter.getValue()))
                .map(connectionParameterValue -> FrameworkCrypto.getInstance().decryptIfNeeded(connectionParameterValue));

    }

}
