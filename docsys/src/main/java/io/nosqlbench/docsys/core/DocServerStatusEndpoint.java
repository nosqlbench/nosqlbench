package io.nosqlbench.docsys.core;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.docsys.api.WebServiceObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Service(WebServiceObject.class)
@Singleton
@Path("_")
public class DocServerStatusEndpoint implements WebServiceObject {
    private final static Logger logger =
            LogManager.getLogger(DocServerStatusEndpoint.class);

    @Context
    private Configuration config;

    private String name;

    @GET
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStats() {
        DocServer s = (DocServer) config.getProperty("server");
        return s.toString();
    }

}
