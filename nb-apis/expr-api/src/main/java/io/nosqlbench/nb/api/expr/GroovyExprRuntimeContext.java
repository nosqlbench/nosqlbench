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


import groovy.lang.Binding;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default runtime context implementation that uses a Groovy {@link Binding} as
 * the storage mechanism for both variables and function adapters.
 */
final class GroovyExprRuntimeContext implements ExprRuntimeContext {

    private final Binding binding;
    private final Optional<URI> sourceUri;
    private final Map<String, ?> parameters;
    private final Map<String, ExprFunctionMetadata> metadata = new LinkedHashMap<>();

    GroovyExprRuntimeContext(Binding binding, Optional<URI> sourceUri, Map<String, ?> parameters) {
        this.binding = Objects.requireNonNull(binding, "binding");
        this.sourceUri = Objects.requireNonNull(sourceUri, "sourceUri");
        this.parameters = Map.copyOf(parameters);
    }

    Binding binding() {
        return binding;
    }

    @Override
    public Optional<URI> sourceUri() {
        return sourceUri;
    }

    @Override
    public Map<String, ?> parameters() {
        return parameters;
    }

    @Override
    public void setVariable(String name, Object value) {
        binding.setVariable(name, value);
    }

    @Override
    public void registerFunction(ExprFunctionMetadata metadata, ExprFunction function) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(function, "function");
        this.metadata.put(metadata.name(), metadata);
        binding.setVariable(metadata.name(), new GroovyExprFunctionAdapter(function));
    }

    @Override
    public Map<String, ExprFunctionMetadata> getRegisteredMetadata() {
        return Map.copyOf(metadata);
    }

    /**
     * Register metadata for a function that is already available in the binding
     * (e.g., from a Groovy script method). This allows library scripts to contribute
     * function metadata without needing to wrap the functions in ExprFunction adapters.
     *
     * @param metadata the function metadata to register
     */
    void registerMetadataOnly(ExprFunctionMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");
        this.metadata.put(metadata.name(), metadata);
    }
}
