package io.metadew.iesi.metadata.configuration.template;

import io.metadew.iesi.common.configuration.metadata.repository.MetadataRepositoryConfiguration;
import io.metadew.iesi.common.configuration.metadata.tables.MetadataTablesConfiguration;
import io.metadew.iesi.connection.database.Database;
import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.metadata.configuration.Configuration;
import io.metadew.iesi.metadata.configuration.template.matcher.MatcherConfiguration;
import io.metadew.iesi.metadata.definition.template.Template;
import io.metadew.iesi.metadata.definition.template.TemplateKey;
import io.metadew.iesi.metadata.definition.template.matcher.Matcher;
import io.metadew.iesi.metadata.definition.template.matcher.MatcherKey;
import io.metadew.iesi.metadata.definition.template.matcher.value.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Log4j2
public class TemplateConfiguration extends Configuration<Template, TemplateKey> {

    private static final String fetchSingleQuery = "SELECT template.id as template_id, template.name as template_name, template.version as template_version, template.description as template_description, matcher.id as matcher_id, " +
            "matcher.key as matcher_key, matcher_value.id as matcher_value_id, any_matcher_value.id as any_matcher_value_id, " +
            "fixed_matcher_value.id as fixed_matcher_value_id, fixed_matcher_value.value as fixed_matcher_value_value, " +
            "templ_matcher_value.id as templ_matcher_value_id, templ_matcher_value.template_name as templ_matcher_value_templ_name, templ_matcher_value.template_version as templ_matcher_value_templ_vrs " +
            "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Templates").getName() + " template " +
            "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Matchers").getName() + " matcher on template.id=matcher.template_id " +
            "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("MatcherValues").getName() + " matcher_value on matcher.id=matcher_value.matcher_id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("AnyMatcherValues").getName() + " any_matcher_value on matcher_value.id=any_matcher_value.id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("FixedMatcherValues").getName() + " fixed_matcher_value on matcher_value.id=fixed_matcher_value.id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("TemplateMatcherValues").getName() + " templ_matcher_value on matcher_value.id=templ_matcher_value.id " +
            "WHERE template.id={0};";
    private static final String fetchByNameAndVersionQuery = "SELECT template.id as template_id, template.name as template_name, template.version as template_version, template.description as template_description, matcher.id as matcher_id, " +
            "matcher.key as matcher_key, matcher_value.id as matcher_value_id, any_matcher_value.id as any_matcher_value_id, " +
            "fixed_matcher_value.id as fixed_matcher_value_id, fixed_matcher_value.value as fixed_matcher_value_value, " +
            "templ_matcher_value.id as templ_matcher_value_id, templ_matcher_value.template_name as templ_matcher_value_templ_name, templ_matcher_value.template_version as templ_matcher_value_templ_vrs " +
            "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Templates").getName() + " template " +
            "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Matchers").getName() + " matcher on template.id=matcher.template_id " +
            "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("MatcherValues").getName() + " matcher_value on matcher.id=matcher_value.matcher_id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("AnyMatcherValues").getName() + " any_matcher_value on matcher_value.id=any_matcher_value.id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("FixedMatcherValues").getName() + " fixed_matcher_value on matcher_value.id=fixed_matcher_value.id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("TemplateMatcherValues").getName() + " templ_matcher_value on matcher_value.id=templ_matcher_value.id " +
            "WHERE template.name={0} and template.version={1};";
    private static final String fetchAllQuery = "SELECT template.id as template_id, template.name as template_name, template.version as template_version, template.description as template_description, matcher.id as matcher_id, " +
            "matcher.key as matcher_key, matcher_value.id as matcher_value_id, any_matcher_value.id as any_matcher_value_id, " +
            "fixed_matcher_value.id as fixed_matcher_value_id, fixed_matcher_value.value as fixed_matcher_value_value, " +
            "templ_matcher_value.id as templ_matcher_value_id, templ_matcher_value.template_name as templ_matcher_value_templ_name, templ_matcher_value.template_version as templ_matcher_value_templ_vrs " +
            "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Templates").getName() + " template " +
            "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Matchers").getName() + " matcher on template.id=matcher.template_id " +
            "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("MatcherValues").getName() + " matcher_value on matcher.id=matcher_value.matcher_id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("AnyMatcherValues").getName() + " any_matcher_value on matcher_value.id=any_matcher_value.id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("FixedMatcherValues").getName() + " fixed_matcher_value on matcher_value.id=fixed_matcher_value.id " +
            "LEFT OUTER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("TemplateMatcherValues").getName() + " templ_matcher_value on matcher_value.id=templ_matcher_value.id ";

    private static final String existsByNameQuery = "SELECT template.id " +
            "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Templates").getName() + " " +
            "WHERE template.name={0};";
    private static final String deleteByTemplateIdQuery = "DELETE FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Templates").getName() + " where id={0});";

    private static final String insertQuery = "INSERT INTO " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Templates").getName() + " (ID, NAME, VERSION, DESCRIPTION) VALUES ({0}, {1}, {2}, {3});";

    private static final String updateQuery = "UPDATE " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("Templates").getName() + " " +
            "SET NAME={0}, VERSION={1}, DESCRIPTION={2} WHERE ID={3};";

    private static TemplateConfiguration INSTANCE;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public synchronized static TemplateConfiguration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TemplateConfiguration();
        }
        return INSTANCE;
    }

    private TemplateConfiguration() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(MetadataRepositoryConfiguration.getInstance()
                .getDesignMetadataRepository()
                .getRepositoryCoordinator()
                .getDatabases().values().stream()
                .findFirst()
                .map(Database::getConnectionPool)
                .orElseThrow(RuntimeException::new));
        setMetadataRepository(MetadataRepositoryConfiguration.getInstance().getDesignMetadataRepository());
    }

    @Override
    public Optional<Template> get(TemplateKey metadataKey) {
        try {
            CachedRowSet cachedRowSet = getMetadataRepository().executeQuery(
                    MessageFormat.format(fetchSingleQuery,
                            SQLTools.GetStringForSQL(metadataKey.getId())
                    ),
                    "reader");
            Template template = null;
            while (cachedRowSet.next()) {
                if (template == null) {
                    template = mapRow(cachedRowSet);
                }
                addMapping(template, cachedRowSet);
            }
            return Optional.ofNullable(template);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(String name) {
        CachedRowSet cachedRowSet = getMetadataRepository().executeQuery(
                MessageFormat.format(existsByNameQuery,
                        SQLTools.GetStringForSQL(name)
                ),
                "reader");
        return cachedRowSet.size() >= 1;
    }

    public Optional<Template> getByNameAndVersion(String name, Long version) {
        try {
            CachedRowSet cachedRowSet = getMetadataRepository().executeQuery(
                    MessageFormat.format(fetchByNameAndVersionQuery,
                            SQLTools.GetStringForSQL(name),
                            SQLTools.GetStringForSQL(version)
                    ),
                    "reader");
            Template template = null;
            while (cachedRowSet.next()) {
                if (template == null) {
                    template = mapRow(cachedRowSet);
                }
                addMapping(template, cachedRowSet);
            }
            return Optional.ofNullable(template);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Template> getAll() {
        return namedParameterJdbcTemplate.query(fetchAllQuery, new TemplateListResultSetExtractor());
    }

    @Override
    public void delete(TemplateKey templateKey) {
        MatcherConfiguration.getInstance().deleteByTemplateId(templateKey);
        getMetadataRepository().executeUpdate(
                MessageFormat.format(deleteByTemplateIdQuery,
                        SQLTools.GetStringForSQL(templateKey.getId())
                ));
    }

    public void deleteByNameAndVersion(String name, long version) {
        getByNameAndVersion(name, version).ifPresent(
                template -> {
                    MatcherConfiguration.getInstance().deleteByTemplateId(template.getMetadataKey());
                    getMetadataRepository().executeUpdate(
                            MessageFormat.format(deleteByTemplateIdQuery,
                                    SQLTools.GetStringForSQL(template.getMetadataKey().getId())
                            ));
                }
        );

    }

    @Override
    public void insert(Template template) {
        getMetadataRepository().executeUpdate(
                MessageFormat.format(insertQuery,
                        SQLTools.GetStringForSQL(template.getMetadataKey().getId()),
                        SQLTools.GetStringForSQL(template.getName()),
                        SQLTools.GetStringForSQL(template.getVersion()),
                        SQLTools.GetStringForSQL(template.getDescription())));
        for (Matcher matcher : template.getMatchers()) {
            MatcherConfiguration.getInstance().insert(matcher);
        }
    }

    public void update(Template template) {
        getMetadataRepository().executeUpdate(
                MessageFormat.format(updateQuery,
                        SQLTools.GetStringForSQL(template.getName()),
                        SQLTools.GetStringForSQL(template.getVersion()),
                        SQLTools.GetStringForSQL(template.getDescription()),
                        SQLTools.GetStringForSQL(template.getMetadataKey().getId())));
        MatcherConfiguration.getInstance().deleteByTemplateId(template.getMetadataKey());
        for (Matcher matcher : template.getMatchers()) {
            MatcherConfiguration.getInstance().insert(matcher);
        }
    }

    private Template mapRow(CachedRowSet cachedRowSet) throws SQLException {
        return Template.builder()
                .metadataKey(
                        TemplateKey.builder()
                                .id(UUID.fromString(cachedRowSet.getString("template_id")))
                                .build())
                .name(cachedRowSet.getString("template_name"))
                .version(Long.parseLong(cachedRowSet.getString("template_version")))
                .description(cachedRowSet.getString("template_description"))
                .matchers(new ArrayList<>())
                .build();
    }

    private void addMapping(Template template, CachedRowSet cachedRowSet) throws SQLException {
        MatcherKey matcherKey = MatcherKey.builder()
                .id(UUID.fromString(cachedRowSet.getString("matcher_id")))
                .build();
        MatcherValue matcherValue;
        if (cachedRowSet.getString("any_matcher_value_id") != null) {
            matcherValue = MatcherAnyValue.builder()
                    .matcherValueKey(MatcherValueKey.builder()
                            .id(UUID.fromString(cachedRowSet.getString("any_matcher_value_id")))
                            .build())
                    .matcherKey(matcherKey)
                    .build();
        } else if (cachedRowSet.getString("fixed_matcher_value_id") != null) {
            matcherValue = MatcherFixedValue.builder()
                    .metadataKey(MatcherValueKey.builder()
                            .id(UUID.fromString(cachedRowSet.getString("fixed_matcher_value_id")))
                            .build())
                    .value(cachedRowSet.getString("fixed_matcher_value_value"))
                    .matcherKey(matcherKey)
                    .build();
        } else if (cachedRowSet.getString("templ_matcher_value_id") != null) {
            matcherValue = MatcherTemplate.builder()
                    .metadataKey(MatcherValueKey.builder()
                            .id(UUID.fromString(cachedRowSet.getString("templ_matcher_value_id")))
                            .build())
                    .templateName(cachedRowSet.getString("templ_matcher_value_templ_name"))
                    .templateVersion(cachedRowSet.getLong("templ_matcher_value_templ_vrs"))
                    .matcherKey(matcherKey)
                    .build();
        } else {
            throw new RuntimeException("Matcher " + matcherKey.toString() + " not of a known type");
        }

        Matcher matcher = Matcher.builder()
                .matcherKey(matcherKey)
                .key(cachedRowSet.getString("matcher_key"))
                .templateKey(template.getMetadataKey())
                .matcherValue(matcherValue)
                .build();
        template.addMatcher(matcher);
    }

}
