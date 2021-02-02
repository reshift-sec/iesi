package io.metadew.iesi.server.rest.dataset.dto;

import io.metadew.iesi.connection.tools.SQLTools;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class DatasetDtoListResultSetExtractor {

    private static final String IN_MEMORY_DATASET_IMPLEMENTATION_TYPE = "in_memory";

    public List<DatasetDto> extractData(CachedRowSet rs) throws SQLException {
        Map<UUID, DatasetDtoBuilder> datasetBuilderMap = new LinkedHashMap<>();
        DatasetDtoBuilder datasetDtoBuilder;
        while (rs.next()) {
            UUID uuid = UUID.fromString(rs.getString("dataset_id"));
            datasetDtoBuilder = datasetBuilderMap.get(uuid);
            if (datasetDtoBuilder == null) {
                datasetDtoBuilder = mapDatasetDtoBuilder(rs);
                datasetBuilderMap.put(uuid, datasetDtoBuilder);
            }

            addImplementation(datasetDtoBuilder, rs);
        }
        return datasetBuilderMap.values().stream().map(DatasetDtoBuilder::build).collect(Collectors.toList());
    }

    public List<DatasetNoImplDto> extractDataOnlyUuid(CachedRowSet rs) throws SQLException {
        List<DatasetOnlyUuidDtoBuilder> datasetBuilderSet = new LinkedList<>();
        DatasetOnlyUuidDtoBuilder datasetDtoBuilder = null;
        while (rs.next()) {
            if (datasetDtoBuilder == null) {
                datasetDtoBuilder = mapDatasetOnlyUuidDtoBuilder(rs);
                datasetBuilderSet.add(datasetDtoBuilder);
            }

            addImplementationOnlyUuid(datasetDtoBuilder,rs);

        }
        return datasetBuilderSet.stream().map(DatasetOnlyUuidDtoBuilder::build).collect(Collectors.toList());
    }

    private void addImplementationOnlyUuid(DatasetOnlyUuidDtoBuilder datasetDtoBuilder, CachedRowSet rs) throws SQLException {
        if (rs.getString("dataset_impl_id") == null) {
            return;
        }
        UUID datasetImplementationId = UUID.fromString(rs.getString("dataset_impl_id"));
        datasetDtoBuilder.getDatasetImplementationBuilders().add(datasetImplementationId);

    }



    private void addImplementation(DatasetDtoBuilder datasetDtoBuilder, CachedRowSet rs) throws SQLException {
        if (rs.getString("dataset_impl_id") == null) {
            return;
        }
        UUID datasetImplementationId = UUID.fromString(rs.getString("dataset_impl_id"));
        DatasetImplementationDtoBuilder datasetImplementationBuilder = datasetDtoBuilder.getDatasetImplementationBuilders().get(datasetImplementationId);
            if (datasetImplementationBuilder == null) {
                datasetImplementationBuilder = extractDatasetImplementationBuilderMapRow(rs);
                datasetDtoBuilder.getDatasetImplementationBuilders().put(datasetImplementationId, datasetImplementationBuilder);
            }

            String type = mapType(rs);

            if (type.equalsIgnoreCase(IN_MEMORY_DATASET_IMPLEMENTATION_TYPE)) {
                mapInMemoryDatasetImplementationDto(rs, (InMemoryDatasetImplementationDtoBuilder) datasetImplementationBuilder);
            } else {
                log.warn("no type found for dataset implementation");
            }

            mapDatasetImplementationLabel(rs, datasetImplementationBuilder);

    }

    private void mapDatasetImplementationLabel(CachedRowSet rs, DatasetImplementationDtoBuilder datasetImplementationBuilder) throws SQLException {
        String datasetImplementationLabelId = rs.getString("dataset_impl_label_id");
        if (datasetImplementationLabelId != null && datasetImplementationBuilder.getDatasetImplementationLabels().get(UUID.fromString(datasetImplementationLabelId)) == null) {
            datasetImplementationBuilder.getDatasetImplementationLabels().put(
                    UUID.fromString(datasetImplementationLabelId),
                    new DatasetImplementationLabelDto(
                            UUID.fromString(datasetImplementationLabelId),
                            rs.getString("dataset_impl_label_value"))
            );
        }
    }

    private void mapInMemoryDatasetImplementationDto(CachedRowSet rs, InMemoryDatasetImplementationDtoBuilder datasetImplementationBuilder) throws SQLException {
        String inMemoryKeyValueId = rs.getString("dataset_in_mem_impl_kv_id");
        if (inMemoryKeyValueId != null && datasetImplementationBuilder.getKeyValues().get(UUID.fromString(inMemoryKeyValueId)) == null) {
            String clobValue = SQLTools.getStringFromSQLClob(rs, "dataset_in_mem_impl_kvs_value");
            datasetImplementationBuilder.getKeyValues().put(UUID.fromString(inMemoryKeyValueId),
                    new InMemoryDatasetImplementationKeyValueDto(
                            UUID.fromString(inMemoryKeyValueId),
                            rs.getString("dataset_in_mem_impl_kvs_key"),
                            clobValue)
            );
        }
    }

    private DatasetImplementationDtoBuilder extractDatasetImplementationBuilderMapRow(CachedRowSet rs) throws SQLException {
        String type = mapType(rs);
        if (type.equalsIgnoreCase(IN_MEMORY_DATASET_IMPLEMENTATION_TYPE)) {
            return extractInMemoryDatasetImplementation(rs);
        } else {
            throw new RuntimeException("cannot create dataset implementation for type " + type);
        }
    }

    private DatasetImplementationDtoBuilder extractInMemoryDatasetImplementation(CachedRowSet rs) throws SQLException {
        return new InMemoryDatasetImplementationDtoBuilder(
                UUID.fromString(rs.getString("dataset_impl_id")),
                new HashMap<>(),
                new HashMap<>()
        );
    }

    private String mapType(CachedRowSet rs) throws SQLException {
        // "dataset_in_mem_impls.ID as dataset_in_mem_impl_id, " +
        if (rs.getString("dataset_in_mem_impl_id") != null) {
            return IN_MEMORY_DATASET_IMPLEMENTATION_TYPE;
        } else {
            throw new RuntimeException("cannot determine the type of dataset_implementation");
        }
    }

    private DatasetDtoBuilder mapDatasetDtoBuilder(CachedRowSet rs) throws SQLException {
        return new DatasetDtoBuilder(
                UUID.fromString(rs.getString("dataset_id")),
                rs.getString("dataset_name"),
                new HashMap<>()
        );
    }

    private DatasetOnlyUuidDtoBuilder mapDatasetOnlyUuidDtoBuilder(CachedRowSet rs) throws SQLException {
        return new DatasetOnlyUuidDtoBuilder(
                UUID.fromString(rs.getString("dataset_id")),
                rs.getString("dataset_name"),
                new HashSet<>()

        );
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class DatasetDtoBuilder {
        private final UUID uuid;
        private final String name;
        private final Map<UUID, DatasetImplementationDtoBuilder> datasetImplementationBuilders;

        public DatasetDto build() {
            return new DatasetDto(uuid, name, datasetImplementationBuilders.values().stream()
                    .map(DatasetImplementationDtoBuilder::build)
                    .collect(Collectors.toSet())
            );
        }
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class DatasetOnlyUuidDtoBuilder {
        private final UUID uuid;
        private final String name;
        private final Set<UUID> datasetImplementationBuilders;

        public DatasetNoImplDto build() {
            return new DatasetNoImplDto(uuid, name,datasetImplementationBuilders);
        }
    }


    @AllArgsConstructor
    @Getter
    @ToString
    public abstract static class DatasetImplementationDtoBuilder {
        private final UUID uuid;
        private final Map<UUID, DatasetImplementationLabelDto> datasetImplementationLabels;

        public abstract DatasetImplementationDto build();

    }

    @AllArgsConstructor
    @Getter
    @ToString
    public abstract static class DatasetImplementationOnlyUuidDtoBuilder {
        private final UUID uuid;
        private final List<UUID> datasetImplementationUuid;

        public abstract DatasetImplementationDto build();

    }


    @Getter
    @ToString(callSuper = true)
    private static class InMemoryDatasetImplementationDtoBuilder extends DatasetImplementationDtoBuilder {

        private final Map<UUID, InMemoryDatasetImplementationKeyValueDto> keyValues;

        public InMemoryDatasetImplementationDtoBuilder(UUID uuid, Map<UUID, DatasetImplementationLabelDto> datasetImplementationLabels, Map<UUID, InMemoryDatasetImplementationKeyValueDto> keyValues) {
            super(uuid, datasetImplementationLabels);
            this.keyValues = keyValues;
        }

        @Override
        public DatasetImplementationDto build() {
            return new InMemoryDatasetImplementationDto(
                    getUuid(),
                    new HashSet<>(getDatasetImplementationLabels().values()),
                    new HashSet<>(getKeyValues().values()));
        }
    }

}
