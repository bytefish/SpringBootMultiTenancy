// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.multitenancy;

import com.zaxxer.hikari.HikariDataSource;
import de.bytefish.multitenancy.routing.TenantAwareRoutingSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class SampleSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleSpringApplication.class, args);
	}

	@Bean
	public DataSource dataSource() {

		AbstractRoutingDataSource dataSource = new TenantAwareRoutingSource();

		Map<Object,Object> targetDataSources = new HashMap<>();

		targetDataSources.put("TenantOne", tenantOne());
		targetDataSources.put("TenantTwo", tenantTwo());

		dataSource.setTargetDataSources(targetDataSources);

		dataSource.afterPropertiesSet();

		return dataSource;
	}

	public DataSource tenantOne() {

		HikariDataSource dataSource = new HikariDataSource();

		dataSource.setInitializationFailTimeout(0);
		dataSource.setMaximumPoolSize(5);
		dataSource.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
		dataSource.addDataSourceProperty("url", "jdbc:postgresql://127.0.0.1:5432/sampledb");
		dataSource.addDataSourceProperty("user", "philipp");
		dataSource.addDataSourceProperty("password", "test_pwd");

		return dataSource;
	}

	public DataSource tenantTwo() {

		HikariDataSource dataSource = new HikariDataSource();

		dataSource.setInitializationFailTimeout(0);
		dataSource.setMaximumPoolSize(5);
		dataSource.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
		dataSource.addDataSourceProperty("url", "jdbc:postgresql://127.0.0.1:5432/sampledb2");
		dataSource.addDataSourceProperty("user", "philipp");
		dataSource.addDataSourceProperty("password", "test_pwd");

		return dataSource;
	}
}