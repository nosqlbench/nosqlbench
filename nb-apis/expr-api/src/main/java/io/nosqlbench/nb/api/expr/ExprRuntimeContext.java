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
import java.util.Map;
import java.util.Optional;

/**
 * Runtime view of the expression evaluation environment that service providers
 * can use to register functions or inspect workload level metadata prior to
 * expression evaluation.
 */
public interface ExprRuntimeContext {
    /**
     * @return the URI for the workload that is currently being processed if one is available
     */
    Optional<URI> sourceUri();

    /**
     * @return immutable view of parameters supplied alongside the workload
     */
    Map<String, ?> parameters();

    /**
     * Set a variable into the Groovy binding prior to evaluation.
     *
     * @param name variable name to expose
     * @param value value to bind to the provided name
     */
    void setVariable(String name, Object value);

    /**
     * Register a function that can be invoked directly from Groovy expressions.
     *
     * @param name name of the function as it will appear to Groovy callers
     * @param function implementation that backs the function call
     */
    void registerFunction(ExprFunctionMetadata metadata, ExprFunction function);

    default void registerFunction(String name, ExprFunction function) {
        registerFunction(new ExprFunctionMetadata(name, "", ""), function);
    }

    default void registerFunction(String name, ExprFunction function, String synopsis, String description) {
        registerFunction(new ExprFunctionMetadata(name, synopsis, description), function);
    }

    Map<String, ExprFunctionMetadata> getRegisteredMetadata();
}
