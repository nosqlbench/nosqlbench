package io.nosqlbench.docsys.core;

import io.nosqlbench.docsys.api.WebServiceObject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

//@Service(value=WebServiceObject.class, selector="test", maturity=Stability.Stable)
@Singleton
@Path("test1")
public class TestServlet1 implements WebServiceObject {
    private final static Logger logger =
            LogManager.getLogger(TestServlet1.class);

//    @Context
//    private Configuration config;

//    private String name;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getStats() {
        return List.of("one","two","three");
    }

    @GET
    @Path("map")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> getMap() {
        return Map.of("key1","value1","key2","value2");
    }

    @GET
    @Path("set")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getSet() {
        return Set.of("one", "two", "three");
    }

}
