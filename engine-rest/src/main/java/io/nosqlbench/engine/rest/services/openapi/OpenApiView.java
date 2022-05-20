package io.nosqlbench.engine.rest.services.openapi;

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


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#pathsObject
 */
public class OpenApiView {

    private final OpenAPI model;

    public OpenApiView(OpenAPI model) {
        this.model = model;
    }

    public OpenAPI getDereferenced() {

        // Since links, parameters, and responses in components can have references,
        // connect their references first

        resolveBodiesInLinks();
        resolveSchemasInParameters();
        resolveHeadersInResponses();

//        OpenAPI out = new OpenAPI();
        Paths paths = model.getPaths();

        for (String pathKey : paths.keySet()) {
            PathItem pi = paths.get(pathKey);
            if (pi.get$ref() != null) {
                throw new RuntimeException("Unable to read external ref in this version for path '" + pathKey + "':'"
                    + pi.get$ref() + "'");
            }

            for (Operation op : new Operation[]{
                pi.getDelete(),
                pi.getGet(),
                pi.getPost(),
                pi.getTrace(),
                pi.getPatch(),
                pi.getOptions(),
                pi.getPut(),
                pi.getHead()
            }) {
                if (op != null) {
                    flattenOperation(op);
                }
            }

            pi.setParameters(resolveParameterList(pi.getParameters()));

        }
        return model;
    }

    private void flattenOperation(Operation op) {
        if (op.getResponses() != null) {
            op.setResponses(resolveResponsesMap(op.getResponses()));
        }
        if (op.getParameters() != null) {
            op.setParameters(resolveParameterList(op.getParameters()));
        }
        if (op.getRequestBody() != null) {
            op.setRequestBody(resolveRequestBody(op.getRequestBody()));
        }
    }

    private RequestBody resolveRequestBody(RequestBody requestBody) {
        while (requestBody.get$ref() != null) {
            requestBody = model.getComponents().getRequestBodies().get(requestBody.get$ref());
        }
        return requestBody;
    }

    private List<Parameter> resolveParameterList(List<Parameter> parameters) {
        if (parameters == null) {
            return null;
        }
        List<Parameter> resolved = new ArrayList<>();
        for (Parameter p : parameters) {
            p = resolve(p);
            p.setSchema(resolveSchema(p.getSchema()));
            resolved.add(p);
        }
        return resolved;
    }

    private final static Map<Class<?>, String> componentPaths =
        Map.of(
            Parameter.class, "#/components/parameters/",
            ApiResponse.class, "#/components/responses/"
        );

    private final static Map<Class<?>, String> mapMethods =
        Map.of(
            Parameter.class, "getParameters",
            ApiResponse.class, "getResponses"
        );


    private <T> T resolve(T aliased) {
        if (aliased == null) {
            return null;
        }

        String typepath = componentPaths.get(aliased.getClass());
        if (typepath == null) {
            throw new RuntimeException("Could not find component path prefix for " + aliased.getClass().getCanonicalName());
        }
        String mapMethod = mapMethods.get(aliased.getClass());
        if (mapMethod == null) {
            throw new RuntimeException("Could not find map method for " + aliased.getClass().getCanonicalName());
        }

        T element = aliased;
        int remaining = 100;
        while (true) {
            if (remaining <= 0) {
                throw new RuntimeException("loop limit reached in resolving element");
            }
            try {
                Method getref = element.getClass().getMethod("get$ref");
                Object invoke = getref.invoke(element);
                if (invoke == null) {
                    return element;
                }
                String refid = invoke.toString();

                int idAt = refid.lastIndexOf("/");
                String name = refid.substring(idAt + 1);
                String prefix = refid.substring(0, idAt + 1);
                if (!prefix.equals(typepath)) {
                    throw new RuntimeException("wrong type path (" + typepath + ") for prefix '" + prefix + "'");
                }
                Method getMap = model.getComponents().getClass().getMethod(mapMethod);
                Object mapobj = getMap.invoke(model.getComponents());
                Map map = (Map) mapobj;
                Object o = map.get(name);
                element = (T) o;
            } catch (Exception e) {
                throw new RuntimeException("unable to call get$ref: " + aliased.getClass().getCanonicalName());
            }
        }
    }

    private Schema resolveSchema(Schema schema) {
        while (schema.get$ref() != null) {
            schema = model.getComponents().getSchemas().get(schema.get$ref());
        }
        return schema;
    }

    private ApiResponses resolveResponsesMap(ApiResponses responses) {
        if (responses != null) {
            for (String rk : responses.keySet()) {
                ApiResponse response = responses.get(rk);
                response = resolve(response);

                response.setHeaders(resolveHeaderMap(response.getHeaders()));
                response.setExtensions(resolveExtensionsMap(response.getExtensions()));
                response.setLinks(resolveLinksMap(response.getLinks()));
            }
        }
        return responses;
    }

    private Map<String, Link> resolveLinksMap(Map<String, Link> links) {
        if (links != null) {
            for (String lk : links.keySet()) {
                Link link = links.get(lk);
                while (link.get$ref() != null) {
                    link = model.getComponents().getLinks().get(link.get$ref());
                }
                links.put(lk, link);
            }
        }
        return links;
    }

    private Map<String, Object> resolveExtensionsMap(Map<String, Object> extensions) {
        if (extensions != null) {
            if (extensions.keySet().size() > 0) {
                throw new RuntimeException("extensions are not supported in this version");
            }
        }
        return extensions;
    }

    private Map<String, Header> resolveHeaderMap(Map<String, Header> headers) {
        if (headers != null) {
            for (String hk : headers.keySet()) {
                Header header = headers.get(hk);
                while (header.get$ref() != null) {
                    header = model.getComponents().getHeaders().get(hk);
                }
                headers.put(hk, header);
            }
        }
        return headers;
    }

    private void resolveBodiesInLinks() {
        Map<String, Link> links = model.getComponents().getLinks();
        if (links == null) {
            return;
        }
        for (String linkKey : links.keySet()) {
            Link modelLink = model.getComponents().getLinks().get(linkKey);

//            RequestBody body = modelLink.getRequestBody();
//            while (body.get$ref() != null) {
//                body = model.getComponents().getRequestBodies().get(body.get$ref());
//            }
            Object body = modelLink.getRequestBody();
            modelLink.setRequestBody(body);
        }
    }

    private void resolveSchemasInParameters() {
        for (String parameterKey : model.getComponents().getParameters().keySet()) {
            Parameter parameter = model.getComponents().getParameters().get(parameterKey);
            Schema schema = parameter.getSchema();
            while (schema.get$ref() != null) {
                schema = model.getComponents().getSchemas().get(schema.get$ref());
            }
            parameter.setSchema(schema);

        }
    }

    private void resolveHeadersInResponses() {
        for (String responseKey : model.getComponents().getResponses().keySet()) {
            ApiResponse response = model.getComponents().getResponses().get(responseKey);
            Map<String, Header> modelHeaders = response.getHeaders();
            Map<String, Header> headers = new HashMap<>();

            for (String headerKey : headers.keySet()) {
                Header header = modelHeaders.get(headerKey);
                while (header.get$ref() != null) {
                    header = modelHeaders.get(header.get$ref());
                }
                headers.put(headerKey, header);
            }
            response.setHeaders(headers);
        }
    }

}
