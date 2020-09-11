package io.nosqlbench.engine.rest.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.Paths;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.models.ParseOptions;
import io.swagger.parser.models.SwaggerParseResult;
import io.swagger.parser.v2.SwaggerConverter;
import io.swagger.util.Json;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service(WebServiceObject.class)
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
            if (filepath == null) {
                filepath = "stargate.yaml";
            } else if (!filepath.endsWith(".yaml")) {
                throw new RuntimeException("Only .yaml filepaths are supported for now.");
            }

            Content<?> one = NBIO.all().name(filepath).one();
            String content = one.asString();
            SwaggerParseResult parsed = parser.readContents(content, List.of(), new ParseOptions());
            List<String> messages = parsed.getMessages();
            if (messages.size() > 0) {
                return Response.serverError().entity(String.join("\n", messages.toArray(new String[0]))).build();
            }

            OpenAPI openAPI = parsed.getOpenAPI();
            Paths paths = openAPI.getPaths();
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();

            for (String pathName : paths.keySet()) {
                PathItem pathItem = paths.get(pathName);

                if (pathItem.getGet() != null) {
                    Operation op = pathItem.getGet();
                    map.put("GET " + pathName, map("GET", pathName, op));
                }
                if (pathItem.getPost() != null) {
                    Operation op = pathItem.getPost();
                    map.put("POST " + pathName, map("POST", pathName, op));
                }
                if (pathItem.getPut() != null) {
                    Operation op = pathItem.getPut();
                    map.put("PUT " + pathName, map("PUT", pathName, op));
                }
                if (pathItem.getPatch() != null) {
                    Operation op = pathItem.getPatch();
                    map.put("PATCH " + pathName, map("PATCH", pathName, op));
                }
                if (pathItem.getDelete() != null) {
                    Operation op = pathItem.getDelete();
                    map.put("DELETE " + pathName, map("DELETE", pathName, op));
                }
                if (pathItem.getHead() != null) {
                    Operation op = pathItem.getHead();
                    map.put("HEAD " + pathName, map("HEAD", pathName, op));
                }
                if (pathItem.getOptions() != null) {
                    Operation op = pathItem.getOptions();
                    map.put("OPTIONS " + pathName, map("OPTIONS", pathName, op));
                }
            }
            return Response.ok(Json.pretty(map)).build();

        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Map<String, Object> map(String method, String pathName, Operation op) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        map.put("method", method);
        map.put("path", pathName);
        map.put("api", op);
        map.put("summary", op.getSummary() != null ? op.getSummary() : "");
        map.put("description", op.getDescription() != null ? op.getDescription() : "");
        return map;

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
