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
