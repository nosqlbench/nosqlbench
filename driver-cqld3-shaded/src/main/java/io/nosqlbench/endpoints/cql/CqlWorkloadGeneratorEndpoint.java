/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
