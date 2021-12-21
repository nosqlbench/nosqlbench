package io.nosqlbench.docsys.endpoints;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.docsys.core.NBWebServer;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.Maturity;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = WebServiceObject.class, selector = "docserver-status", maturity = Maturity.Deprecated)
@Singleton
@Path("_")
public class DocServerStatusEndpoint implements WebServiceObject {

    private final static Logger logger =
            LogManager.getLogger(DocServerStatusEndpoint.class);

    @Context
    private Configuration config;

    private String name;

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStats() {
        NBWebServer s = (NBWebServer) config.getProperty("server");
        return s.toString();
    }

}
