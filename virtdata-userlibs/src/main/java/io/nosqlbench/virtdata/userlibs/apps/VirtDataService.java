package io.nosqlbench.virtdata.userlibs.apps;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.ResolverDiagnostics;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.docsys.api.WebServiceObject;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Service(WebServiceObject.class)
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
