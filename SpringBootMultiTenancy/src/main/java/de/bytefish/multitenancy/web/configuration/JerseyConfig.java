// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.multitenancy.web.configuration;


import de.bytefish.multitenancy.web.filters.TenantNameFilter;
import de.bytefish.multitenancy.web.resources.CustomerResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * Jersey Configuration (Resources, Modules, Filters, ...)
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {

        // Register the Filters:
        register(TenantNameFilter.class);

        // Register the Resources:
        register(CustomerResource.class);

        // Disable WADL Generation:
        property("jersey.config.server.wadl.disableWadl", true);

        // Add some Tracing:
        property("jersey.config.server.tracing.type", "ALL");
        property("jersey.config.server.tracing.threshold", "TRACE");
    }
}
