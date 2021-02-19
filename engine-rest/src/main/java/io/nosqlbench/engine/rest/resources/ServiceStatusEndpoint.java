package io.nosqlbench.engine.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.AutoDocsWebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
