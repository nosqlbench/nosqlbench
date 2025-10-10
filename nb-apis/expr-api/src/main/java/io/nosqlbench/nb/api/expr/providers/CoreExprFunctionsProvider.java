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
import io.nosqlbench.nb.api.expr.annotations.ExprExampleContext;
import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Provides core utility functions that are generally useful for workload preprocessing.
 */
@Service(value = ExprFunctionProvider.class, selector = "core")
public class CoreExprFunctionsProvider implements ExprFunctionProvider {

    @ExprExample(args = {"\"NB_MISSING_ENV\""}, expectNull = true)
    @ExprExample(args = {"\"NB_MISSING_ENV\"", "\"fallback\""}, expect = "\"fallback\"")
    @ExprFunctionSpec(
        name = "env",
        synopsis = "env(name[, default])",
        description = "Return the value of an environment variable or the provided default if unset."
    )
    private Object env(Object[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("env(name[, default]) requires at least a name");
        }
        String name = String.valueOf(args[0]);
        String value = System.getenv(name);
        if (value == null && args.length > 1) {
            return args[1];
        }
        return value;
    }

    @ExprExample(
        args = {"\"nb.expr.test\"", "\"fallback\""},
        expect = "\"value\"",
        systemProperties = {"nb.expr.test=value"}
    )
    @ExprExample(
        args = {"\"nb.expr.missing\"", "\"fallback\""},
        expect = "\"fallback\"",
        systemProperties = {"nb.expr.missing="}
    )
    @ExprFunctionSpec(
        name = "prop",
        synopsis = "prop(name[, default])",
        description = "Return the value of a JVM system property or the provided default if unset."
    )
    private Object prop(Object[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("prop(name[, default]) requires at least a name");
        }
        String name = String.valueOf(args[0]);
        String value = System.getProperty(name);
        if (value == null && args.length > 1) {
            return args[1];
        }
        return value;
    }

    @ExprExample(matches = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    @ExprExample(expectNotNull = true)
    @ExprFunctionSpec(
        name = "uuid",
        synopsis = "uuid()",
        description = "Generate a random UUID in string form."
    )
    private Object uuid() {
        return UUID.randomUUID().toString();
    }

    @ExprExample(matches = "^\\d{4}-\\d{2}-\\d{2}T.*Z$")
    @ExprExample(expectNotNull = true)
    @ExprFunctionSpec(
        name = "now",
        synopsis = "now()",
        description = "Return the current instant in ISO-8601 format."
    )
    private Object now() {
        return Instant.now().toString();
    }

    @ExprExample(args = {"\"value\""}, expect = "\"VALUE\"")
    @ExprExample(args = {"\"123abc\""}, expect = "\"123ABC\"")
    @ExprFunctionSpec(
        name = "upper",
        synopsis = "upper(value)",
        description = "Uppercase the provided value using the ROOT locale."
    )
    private Object upper(Object[] args) {
        return transformCase(args, true);
    }

    @ExprExample(args = {"\"VALUE\""}, expect = "\"value\"")
    @ExprExample(args = {"\"MixEd\""}, expect = "\"mixed\"")
    @ExprFunctionSpec(
        name = "lower",
        synopsis = "lower(value)",
        description = "Lowercase the provided value using the ROOT locale."
    )
    private Object lower(Object[] args) {
        return transformCase(args, false);
    }

    @ExprExample(expect = "\"nb://example\"")
    @ExprExample(expect = "\"\"", context = ExprExampleContext.NO_SOURCE_URI)
    @ExprFunctionSpec(
        name = "source",
        synopsis = "source()",
        description = "Return the source URI for the current workload, if available."
    )
    private Object source(ExprRuntimeContext context) {
        return context.sourceUri().map(Object::toString).orElse("");
    }

    private Object transformCase(Object[] args, boolean upper) {
        if (args.length != 1) {
            throw new IllegalArgumentException((upper ? "upper" : "lower") + "(value) requires exactly one argument");
        }
        String value = String.valueOf(args[0]);
        return upper ? value.toUpperCase(Locale.ROOT) : value.toLowerCase(Locale.ROOT);
    }
}
