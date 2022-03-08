/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.rest.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.rest.services.openapi.OpenApiLoader;
import io.nosqlbench.nb.annotations.Service;
import io.swagger.parser.OpenAPIParser;
import io.swagger.util.Json;
import io.swagger.v3.parser.converter.SwaggerConverter;
import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Service(value = WebServiceObject.class, selector = "openapi")
@Singleton
@Path("/openapi")
public class OpenApiEndpoint implements WebServiceObject {

    private final OpenAPIParser parser = new OpenAPIParser();
    private final SwaggerConverter converter = new SwaggerConverter();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Json sjson = new Json();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("generate")
    public Response putWorkload(String input) {
        try {
            return Response
                .ok("received " + input.length() + " length request. Phase 2 implemention on the way...")
                .build();
        } catch (Exception e) {
            return Response
                .serverError()
                .entity(e.getMessage())
                .build();
        }
    }

    @GET
    @Path("paths")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPaths(@QueryParam("filepath") String filepath) {

        try {
            Map<String, Object> map = OpenApiLoader.parseToMap(filepath);
            return Response.ok(Json.pretty(map)).build();

        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


//    private String toJson(String method, String path, Operation op) {
//        try {
//            LinkedHashMap<String,Object> map = new LinkedHashMap<>();
//            map.put("id", method.toUpperCase() + " " + path);
//            map.put("path", path);
//            map.put("method", method);
//            map.put("description", op.getDescription());
//            map.put("summary",op.getSummary());
//            map.put("body",op.getRequestBody());
//            map.put("parameters",op.getParameters());
//            map.put("operation_id",op.getOperationId());
//            map.put("external_docs",op.getExternalDocs());
////            return gson.toJson(map);
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}
