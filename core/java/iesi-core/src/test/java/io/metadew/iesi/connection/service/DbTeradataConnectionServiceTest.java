package io.metadew.iesi.connection.service;

import io.metadew.iesi.connection.database.TeradataDatabase;
import io.metadew.iesi.connection.database.connection.DatabaseConnectionHandlerImpl;
import io.metadew.iesi.connection.database.connection.teradata.TeradataDatabaseConnection;
import io.metadew.iesi.connection.operation.DbTeradataConnectionService;
import io.metadew.iesi.metadata.definition.connection.Connection;
import io.metadew.iesi.metadata.definition.connection.ConnectionParameter;
import io.metadew.iesi.metadata.definition.connection.key.ConnectionKey;
import io.metadew.iesi.metadata.definition.connection.key.ConnectionParameterKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;


class DbTeradataConnectionServiceTest {
    @Test
    void getDatabaseTest(){
        DatabaseConnectionHandlerImpl databaseConnectionHandler= DatabaseConnectionHandlerImpl.getInstance();
        DatabaseConnectionHandlerImpl spyList = Mockito.spy(databaseConnectionHandler);

        Mockito.doReturn(null).when(spyList).getConnection(any());

        Connection connection = new Connection(new ConnectionKey("test", "tst"),
                "jdbc:teradata://",
                "description",
                Stream.of(new ConnectionParameter(new ConnectionParameterKey("test", "tst", "host"), "value"),
                        new ConnectionParameter(new ConnectionParameterKey("test", "tst", "database"), "value"),
                        new ConnectionParameter(new ConnectionParameterKey("test", "tst", "user"), "value"),
                        new ConnectionParameter(new ConnectionParameterKey("test", "tst", "password"), "value"))
                        .collect(Collectors.toList()));
     TeradataDatabase teradataDatabaseExpected = new TeradataDatabase(new TeradataDatabaseConnection("value", 0, "value", "value", "value"));
        assertEquals(teradataDatabaseExpected, DbTeradataConnectionService.getInstance().getDatabase(connection));
    }



}