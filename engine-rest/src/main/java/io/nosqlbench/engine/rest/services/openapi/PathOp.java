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


import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

public class PathOp {

    private final String method;
    private final String path;
    private final Operation op;

    public PathOp(String method, String path, Operation op) {
        this.method = method;
        this.path = path;
        this.op = op;
    }

    public Operation getOp() {
        return op;
    }

    public static List<PathOp> wrap(String path, PathItem item) {
        List<String> methods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD", "TRACE");
        Operation[] ops = new Operation[]{item.getGet(), item.getPost(), item.getPut(), item.getDelete(),
            item.getPatch(), item.getOptions(),
            item.getHead(), item.getTrace()};
        List<PathOp> pathops = new ArrayList<>();

        for (int i = 0; i < methods.size(); i++) {
            PathOp pathop = wrap(path, methods.get(i), ops[i]);
            if (pathop != null) {
                pathops.add(pathop);
            }
        }
        return pathops;
    }

    private static PathOp wrap(String path, String method, Operation op) {
        if (op == null) {
            return null;
        }
        return new PathOp(method, path, op);
    }

    public Operation getOperation() {
        return this.op;
    }

    public String toString() {
        String call = getMethod() + " " + getPath();
        if (getOperation().getParameters().size() > 0) {
            for (Parameter p : getOperation().getParameters()) {
                String name = p.getName();
                System.out.println("name: " + name);
            }
        }
        return call;
    }

    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

    public String getCall() {
        return method.toUpperCase() + " " + path;
    }
}
