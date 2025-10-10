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


/**
 * Functional contract for a function that can be exposed to Groovy expressions.
 * Implementations are expected to provide side-effect free logic when possible.
 */
@FunctionalInterface
public interface ExprFunction {
    /**
     * Apply the function to the provided argument list.
     *
     * @param args ordered arguments supplied from the expression invocation
     * @return the value to substitute into the rendered workload
     */
    Object apply(Object... args);
}
