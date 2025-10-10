package io.nosqlbench.nb.api.expr.providers;

/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.expr.ExprFunctionProvider;
import io.nosqlbench.nb.api.expr.ExprRuntimeContext;
import io.nosqlbench.nb.api.expr.annotations.ExprExample;
import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec;

import java.util.Map;

/**
 * Provides helpers for accessing workload parameters from Groovy expressions.
 */
@Service(value = ExprFunctionProvider.class, selector = "parameters")
public class ParameterExprFunctionsProvider implements ExprFunctionProvider {

    @ExprExample(args = {"\"mode\""}, expect = "\"strict\"")
    @ExprExample(args = {"\"threshold\""}, expect = "42")
    @ExprFunctionSpec(
        name = "param",
        synopsis = "param(name)",
        description = "Return the value of a required workload parameter, raising an error when missing."
    )
    private Object param(ExprRuntimeContext context, Object[] args) {
        Map<String, ?> params = context.parameters();
        if (args.length != 1) {
            throw new IllegalArgumentException("param(name) requires exactly one argument");
        }
        String name = String.valueOf(args[0]);
        if (!params.containsKey(name)) {
            throw new IllegalArgumentException("Parameter '" + name + "' was not provided");
        }
        return params.get(name);
    }

    @ExprExample(args = {"\"threshold\"", "\"fallback\""}, expect = "42")
    @ExprExample(args = {"\"missing\"", "\"fallback\""}, expect = "\"fallback\"")
    @ExprFunctionSpec(
        name = "paramOr",
        synopsis = "paramOr(name, default)",
        description = "Return the value of a workload parameter or the provided default when absent or null."
    )
    private Object paramOr(ExprRuntimeContext context, Object[] args) {
        Map<String, ?> params = context.parameters();
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("paramOr(name, default?) expects one or two arguments");
        }
        String name = String.valueOf(args[0]);
        Object value = params.get(name);
        if (value == null && args.length == 2) {
            return args[1];
        }
        return value;
    }

    @ExprExample(args = {"\"mode\""}, expect = "true")
    @ExprExample(args = {"\"missing\""}, expect = "false")
    @ExprFunctionSpec(
        name = "hasParam",
        synopsis = "hasParam(name)",
        description = "Check if a workload parameter was provided."
    )
    private Object hasParam(ExprRuntimeContext context, Object[] args) {
        Map<String, ?> params = context.parameters();
        if (args.length != 1) {
            throw new IllegalArgumentException("hasParam(name) requires exactly one argument");
        }
        String name = String.valueOf(args[0]);
        return params.containsKey(name);
    }
}
