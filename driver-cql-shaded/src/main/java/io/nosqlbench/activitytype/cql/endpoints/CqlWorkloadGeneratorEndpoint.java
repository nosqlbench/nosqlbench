package io.nosqlbench.activitytype.cql.endpoints;

import io.nosqlbench.generators.cql.lang.CqlWorkloadGen;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.annotations.Service;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Service(WebServiceObject.class)
@Path("/services/cql/")
public class CqlWorkloadGeneratorEndpoint implements WebServiceObject {

    @POST
    @Path("generate")
    public Response generate(String cqlSchema) {
        try {
            CqlWorkloadGen generator = new CqlWorkloadGen();
            String generated = generator.generate(cqlSchema);
            return Response.ok(generated).type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }

    }
}
