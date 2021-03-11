package io.nosqlbench.virtdata.userlibs.apps;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.ResolverDiagnostics;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Service(value = WebServiceObject.class, selector = "virtdata")
@Singleton
@Path("virtdata")
public class VirtDataService implements WebServiceObject {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("testrecipe")
    public String parseResult(@QueryParam("recipe") String recipe) {
        try {
            DataMapper<Object> mapper = VirtData.getMapper(recipe);
            return "Success:" + mapper.toString();
        } catch (Exception e) {
            ResolverDiagnostics mapperDiagnostics = VirtData.getMapperDiagnostics(recipe);

            return "There was an error:\n" +
                    e.getMessage() + "\n" +
                    "diagnostics:" + mapperDiagnostics.toString();
        }
    }
}
