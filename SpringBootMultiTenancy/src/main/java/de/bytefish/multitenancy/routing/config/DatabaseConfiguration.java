// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.multitenancy.routing.config;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DatabaseConfiguration {

    private final String tenant;
    private final String url;
    private final String user;
    private final String dataSourceClassName;
    private final String password;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DatabaseConfiguration(@JsonProperty("tenant") String tenant,
            @JsonProperty("url") String url,
            @JsonProperty("user") String user,
            @JsonProperty("dataSourceClassName") String dataSourceClassName,
            @JsonProperty("password") String password) {
        this.tenant = tenant;
        this.url = url;
        this.user = user;
        this.dataSourceClassName = dataSourceClassName;
        this.password = password;
    }

    @JsonProperty("tenant")
    public String getTenant() {
        return tenant;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    @JsonProperty("dataSourceClassName")
    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseConfiguration that = (DatabaseConfiguration) o;
        return Objects.equals(tenant, that.tenant) &&
                Objects.equals(url, that.url) &&
                Objects.equals(user, that.user) &&
                Objects.equals(dataSourceClassName, that.dataSourceClassName) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, url, user, dataSourceClassName, password);
    }
}
