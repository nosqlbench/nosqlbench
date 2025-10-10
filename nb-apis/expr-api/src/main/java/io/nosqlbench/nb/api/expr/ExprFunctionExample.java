package io.nosqlbench.nb.api.expr;

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


import io.nosqlbench.nb.api.expr.annotations.ExprExample;
import io.nosqlbench.nb.api.expr.annotations.ExprExampleContext;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Immutable representation of an example that can be used to exercise an expression helper
 * function.
 */
public record ExprFunctionExample(
    String description,
    List<String> args,
    String expect,
    String matches,
    boolean expectNull,
    boolean expectNotNull,
    List<String> systemProperties,
    ExprExampleContext context
) {

    public ExprFunctionExample {
        args = args == null ? List.of() : List.copyOf(args);
        systemProperties = systemProperties == null ? List.of() : List.copyOf(systemProperties);
        context = Objects.requireNonNullElse(context, ExprExampleContext.DEFAULT);
    }

    public static ExprFunctionExample fromAnnotation(ExprExample example) {
        return new ExprFunctionExample(
            example.description(),
            List.of(example.args()),
            example.expect(),
            example.matches(),
            example.expectNull(),
            example.expectNotNull(),
            List.of(example.systemProperties()),
            example.context()
        );
    }

    /**
     * Human friendly representation suitable for CLI listings.
     */
    public String render() {
        return render("");
    }

    public String render(String functionName) {
        String argsPart = args.isEmpty() ? "()" : args.stream().collect(Collectors.joining(", ", "(", ")"));
        StringBuilder sb = new StringBuilder();
        if (functionName != null && !functionName.isBlank()) {
            sb.append(functionName);
        }
        sb.append(argsPart);

        String expectation;
        if (!expect().isBlank()) {
            expectation = "=> " + expect();
        } else if (!matches().isBlank()) {
            expectation = "~> matches /" + matches() + "/";
        } else if (expectNull()) {
            expectation = "=> null";
        } else if (expectNotNull()) {
            expectation = "=> not null";
        } else {
            expectation = "";
        }
        if (!expectation.isBlank()) {
            sb.append(' ').append(expectation);
        }

        List<String> qualifiers = new java.util.ArrayList<>();
        if (!description().isBlank()) {
            qualifiers.add(description());
        }
        if (context() != ExprExampleContext.DEFAULT) {
            qualifiers.add("context=" + context());
        }
        if (!systemProperties().isEmpty()) {
            String props = systemProperties().stream().collect(Collectors.joining(", "));
            qualifiers.add("props=" + props);
        }
        if (!qualifiers.isEmpty()) {
            sb.append(" [").append(String.join("; ", qualifiers)).append(']');
        }

        return sb.toString();
    }
}
