/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.AutoDocsWebService;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Service(value = WebServiceObject.class, selector = "service-status")
@Singleton
@Path("/services/status")
public class ServiceStatusEndpoint implements WebServiceObject {
    private final static Logger logger = LogManager.getLogger(AutoDocsWebService.class);

    @Context
    private Configuration config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)

    public Response isEnabled(@QueryParam("enabled") String overideEnabled) {
        boolean enabled = false;
        try {
            if (overideEnabled != null) {
                enabled = Boolean.parseBoolean(overideEnabled);
            }
            StatusEncoding status = new StatusEncoding(true, Map.of(
                "status", "ORIGIN/services/status"
            ));
            return Response.ok(status).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private final static class StatusEncoding {
        @JsonProperty("enabled")
        public boolean isEnabled() {
            return enabled;
        }

        @JsonProperty("endpoints")
        public Map<String, String> getEndpoints() {
            return endpoints;
        }

        private final boolean enabled;
        private final Map<String, String> endpoints;

        public StatusEncoding(boolean enabled, Map<String, String> endpoints) {
            this.enabled = enabled;
            this.endpoints = endpoints;
        }
    }
}
