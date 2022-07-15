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

package io.nosqlbench.engine.rest.services.openapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @see <A href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.1.0
 * .md#securityRequirementObject">OpenApi Spec 3.1.0</A>
 */
public class OpenApiLoader {

    private static final OpenAPIParser parser = new OpenAPIParser();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * If it is not found, the Stargate path is used by default.
     *
     * For now, the json is used as a filter with the following conventions:
     * <pre>{@code
     *   {
     *       "<method> <path>": {...},
     *       ...
     *   }
     * }</pre>
     *
     * The presence of a key with a matching method and path to one of those found
     * in the openapi details indicates it should be included.
     *
     * @param openIdYamlPath a filepath is where an openapi descriptor can be found
     * @param json           The selection data used to filer in our out calls from the openapi descriptor
     * @return A yaml workload which can be used with the http driver
     */
    public static String generateWorkloadFromFilepath(String openIdYamlPath, String json) {
        Map<?, ?> filter = gson.fromJson(json, Map.class);
        Set<String> included = filter.keySet().stream()
            .map(Object::toString)
            .collect(Collectors.toSet());

        OpenAPI openAPI = parseOpenApi(openIdYamlPath);
        Paths paths = openAPI.getPaths();

        List<PathOp> allOps = new ArrayList<>();

        for (String pathName : paths.keySet()) {
            PathItem pathItem = paths.get(pathName);
            List<PathOp> calls = PathOp.wrap(pathName, pathItem);
            allOps.addAll(calls);
        }

        List<PathOp> activeOps = allOps.stream()
            .filter(op -> {
                return included.contains(op.getCall());
            })
            .collect(Collectors.toList());

        Map<String, PathOp> pathops = new HashMap<>();
        DumperOptions dumper = new DumperOptions();
        dumper.setAllowReadOnlyProperties(true);
        Yaml yaml = new Yaml(dumper);
        yaml.setBeanAccess(BeanAccess.DEFAULT);

        for (PathOp activeOp : activeOps) {
            System.out.println("yaml for op:" + yaml.dump(activeOp));
            pathops.put(activeOp.getCall(), activeOp);
        }

        String dump = yaml.dump(pathops);
        return dump;
    }

    public static OpenAPI parseOpenApi(String filepath) {
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
            throw new RuntimeException("error while parsing: " + String.join("\n", messages.toArray(new String[0])));
        }

        OpenAPI openAPI = parsed.getOpenAPI();
        return new OpenApiView(openAPI).getDereferenced();
    }

    // TODO; use the op wrapper interface here
    public static Map<String, Object> parseToMap(String filepath) {
        OpenAPI openAPI = parseOpenApi(filepath);

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
        return map;
    }

    private static Map<String, Object> map(String method, String pathName, Operation op) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        map.put("method", method);
        map.put("path", pathName);
        map.put("api", op);
        map.put("summary", op.getSummary() != null ? op.getSummary() : "");
        map.put("description", op.getDescription() != null ? op.getDescription() : "");
        return map;

    }

}
