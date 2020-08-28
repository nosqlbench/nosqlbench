package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.AutoDocsWebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Service(WebServiceObject.class)
@Singleton
@Path("/services/status")
public class ServiceStatusEndpoint implements WebServiceObject {
    private final static Logger logger = LogManager.getLogger(AutoDocsWebService.class);

    @Context
    private Configuration config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isEnabled() {
        return true;
    }

}
