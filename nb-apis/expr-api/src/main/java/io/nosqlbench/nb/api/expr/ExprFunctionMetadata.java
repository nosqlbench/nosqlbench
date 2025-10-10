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


import java.util.List;
import java.util.Objects;

/**
 * Describes a function that can be contributed to the Groovy expression runtime. Metadata is
 * used to produce discoverable listings for users and tooling.
 */
public record ExprFunctionMetadata(
    String name,
    String synopsis,
    String description,
    List<ExprFunctionExample> examples,
    String provider
) {

    public ExprFunctionMetadata {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(synopsis, "synopsis");
        Objects.requireNonNull(description, "description");
        examples = examples == null ? List.of() : List.copyOf(examples);
        provider = provider == null ? "" : provider;
    }

    public ExprFunctionMetadata(String name, String synopsis, String description) {
        this(name, synopsis, description, List.of(), "");
    }

    public ExprFunctionMetadata(String name, String synopsis, String description, List<ExprFunctionExample> examples) {
        this(name, synopsis, description, examples, "");
    }

}
