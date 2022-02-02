    package io.nosqlbench.endpoints.cql;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.generators.cql.lang.CqlWorkloadGen;
import io.nosqlbench.nb.annotations.Service;
import jakarta.inject.Singleton;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Service(value = WebServiceObject.class, selector = "cql-workload-generator")
@Path("/services/cql/")
@Singleton
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
