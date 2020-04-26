// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.multitenancy.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import de.bytefish.multitenancy.core.ThreadLocalStorage;
import de.bytefish.multitenancy.routing.config.DatabaseConfiguration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DynamicTenantAwareRoutingSource extends AbstractRoutingDataSource {

    private final String filename;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, HikariDataSource> tenants;

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

    private ConcurrentMap<String, HikariDataSource> getDataSources() {

        // Deserialize the JSON:
        DatabaseConfiguration[] configurations = getDatabaseConfigurations();

        // Now create a Lookup Table:
        return Arrays
                .stream(configurations)
                .collect(Collectors.toConcurrentMap(x -> x.getTenant(), x -> buildDataSource(x)));
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
    public void refreshDataSources() {
        DatabaseConfiguration[] configurations = getDatabaseConfigurations();

        removeObsoleteTenants(configurations);
        insertOrUpdateTenants(configurations);
    }

    private void insertOrUpdateTenants(DatabaseConfiguration[] configurations) {
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
    private void removeObsoleteTenants(DatabaseConfiguration[] configurations) {

        // Are there Tenants, that have been removed:
        Set<String> tenantNamesFromConfiguration = Arrays.stream(configurations)
                .map(x -> x.getTenant())
                .collect(Collectors.toSet());

        for (String tenant : tenants.keySet()) {

            // There is currently a Tenant, which is not listed anymore:
            if(!tenantNamesFromConfiguration.contains(tenant)) {

                // So get the DataSource first ...
                HikariDataSource dataSource = tenants.get(tenant);

                // ... close all existing connections:
                dataSource.close();

                // ... and remove it:
                tenants.remove(tenant);
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
