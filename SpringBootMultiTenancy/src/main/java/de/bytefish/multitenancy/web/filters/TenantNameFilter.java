// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.multitenancy.web.filters;

import de.bytefish.multitenancy.core.ThreadLocalStorage;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class TenantNameFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {

        MultivaluedMap<String, String> headers = ctx.getHeaders();

        if(headers == null) {
            return;
        }

        if(!headers.containsKey("X-TenantID")) {
            return;
        }

        String tenantName = headers.getFirst("X-TenantID");

        if(tenantName == null) {
            return;
        }

        // Set in the Thread Context of the Request:
        ThreadLocalStorage.setTenantName(tenantName);
    }
}
