package io.metadew.iesi.metadata.configuration.environment;

import io.metadew.iesi.metadata.definition.environment.EnvironmentParameter;
import io.metadew.iesi.metadata.definition.environment.key.EnvironmentKey;
import io.metadew.iesi.metadata.definition.environment.key.EnvironmentParameterKey;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentParameterExtractor implements ResultSetExtractor<List<EnvironmentParameter>> {

    @Override
    public List<EnvironmentParameter> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, EnvironmentParameter> environmentParameterHashMap = new HashMap<>();
        EnvironmentParameter environmentParameter;
        List<EnvironmentParameter> environmentList = new ArrayList<>();
        while (rs.next()) {
            String name = rs.getString("ENV_NM");
            environmentParameter = environmentParameterHashMap.get(name);
            if (environmentParameter == null) {
                environmentParameter = mapRow(rs);
                environmentParameterHashMap.put(name, environmentParameter);
            }
            environmentParameter = mapRow(rs);
            environmentList.add(environmentParameter);
        }
        return environmentList;
    }

    private EnvironmentParameter mapRow(ResultSet rs) throws SQLException {
        EnvironmentKey environmentKey = EnvironmentKey.builder().name(rs.getString("ENV_NM")).build();
        return EnvironmentParameter.builder()
                .environmentParameterKey(
                        EnvironmentParameterKey.builder()
                                .environmentKey(environmentKey)
                                .parameterName(rs.getString("ENV_PAR_NM"))
                                .build())
                .value(rs.getString("ENV_PAR_VAL"))
                .build();
    }
}