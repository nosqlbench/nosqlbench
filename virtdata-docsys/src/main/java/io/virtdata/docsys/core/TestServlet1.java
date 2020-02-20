package io.virtdata.docsys.core;

import io.virtdata.docsys.api.WebServiceObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;

//@Service(WebServiceObject.class)
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
