package de.bytefish.multitenancy.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import de.bytefish.multitenancy.core.ThreadLocalStorage;
import de.bytefish.multitenancy.routing.config.DatabaseConfiguration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DynamicTenantAwareRoutingSource extends AbstractRoutingDataSource {

    private final String filename;
    private final ObjectMapper objectMapper;
    private final Map<String, HikariDataSource> tenants;

    public DynamicTenantAwareRoutingSource(String filename) {
        this(filename, new ObjectMapper());
    }

    public DynamicTenantAwareRoutingSource(String filename, ObjectMapper objectMapper) {
        this.filename = filename;
        this.objectMapper = objectMapper;
        this.tenants = getDataSources();
    }

    @Override
    public void afterPropertiesSet() {
        // Nothing to do ..
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String lookupKey = (String) determineCurrentLookupKey();

        // And finally return it:
        return tenants.get(lookupKey);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return ThreadLocalStorage.getTenantName();
    }

    private Map<String, HikariDataSource> getDataSources() {

        // Deserialize the JSON:
        DatabaseConfiguration[] configurations = getDatabaseConfigurations();

        // Now create a Lookup Table:
        return Arrays
                .stream(configurations)
                .collect(Collectors.toMap(x -> x.getTenant(), x -> buildDataSource(x)));
    }

    private DatabaseConfiguration[] getDatabaseConfigurations() {
        try {
            return objectMapper.readValue(new File(filename), DatabaseConfiguration[].class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HikariDataSource buildDataSource(DatabaseConfiguration configuration) {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setInitializationFailTimeout(0);
        dataSource.setMaximumPoolSize(5);
        dataSource.setDataSourceClassName(configuration.getDataSourceClassName());
        dataSource.addDataSourceProperty("url", configuration.getUrl());
        dataSource.addDataSourceProperty("user", configuration.getUser());
        dataSource.addDataSourceProperty("password", configuration.getPassword());

        return dataSource;
    }

    @Scheduled(fixedDelay = 5000L)
    public void insertOrUpdateDataSources() {

        DatabaseConfiguration[] configurations = getDatabaseConfigurations();

        for (DatabaseConfiguration configuration : configurations) {
            if (tenants.containsKey(configuration.getTenant())) {
                HikariDataSource dataSource = tenants.get(configuration.getTenant());
                // We only shutdown and reload, if the configuration has actually changed...
                if (!isCurrentConfiguration(dataSource, configuration)) {
                    // Make sure we close this DataSource first...
                    dataSource.close();
                    // ... and then insert a new DataSource:
                    tenants.put(configuration.getTenant(), buildDataSource(configuration));
                }
            } else {
                tenants.put(configuration.getTenant(), buildDataSource(configuration));
            }
        }
    }

    private boolean isCurrentConfiguration(HikariDataSource dataSource, DatabaseConfiguration configuration) {
        return Objects.equals(dataSource.getDataSourceProperties().getProperty("user"), configuration.getUser())
                && Objects.equals(dataSource.getDataSourceProperties().getProperty("url"), configuration.getUrl())
                && Objects.equals(dataSource.getDataSourceProperties().getProperty("password"), configuration.getPassword())
                && Objects.equals(dataSource.getDataSourceClassName(), configuration.getDataSourceClassName());
    }
}
