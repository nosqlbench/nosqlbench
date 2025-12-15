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


import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TestExprRuntimeContext implements ExprRuntimeContext {

    private final Optional<URI> sourceUri;
    private final Map<String, ?> parameters;
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, ExprFunction> functions = new HashMap<>();
    private final Map<String, ExprFunctionMetadata> metadata = new HashMap<>();

    public TestExprRuntimeContext(Map<String, ?> parameters, Optional<URI> sourceUri) {
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
        this.sourceUri = Objects.requireNonNull(sourceUri, "sourceUri");
    }

    public TestExprRuntimeContext(Map<String, ?> parameters) {
        this(parameters, Optional.empty());
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
        variables.put(name, value);
    }

    @Override
    public void registerFunction(ExprFunctionMetadata metadata, ExprFunction function) {
        functions.put(metadata.name(), function);
        this.metadata.put(metadata.name(), metadata);
    }

    @Override
    public Map<String, ExprFunctionMetadata> getRegisteredMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public ExprFunction function(String name) {
        return functions.get(name);
    }

    @Override
    public Object getVariable(String name) {
        return variables.get(name);
    }

    @Override
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }
}
