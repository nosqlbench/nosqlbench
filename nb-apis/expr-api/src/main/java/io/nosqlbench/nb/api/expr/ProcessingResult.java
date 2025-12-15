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

import java.util.Set;

/**
 * Result of processing a workload source with expression evaluation,
 * including the transformed output and the final binding context.
 */
public class ProcessingResult {
    private final String output;
    private final Binding binding;
    private final Set<String> initialVariables;

    public ProcessingResult(String output, Binding binding) {
        this(output, binding, Set.of());
    }

    public ProcessingResult(String output, Binding binding, Set<String> initialVariables) {
        this.output = output;
        this.binding = binding;
        this.initialVariables = Set.copyOf(initialVariables);
    }

    /**
     * @return the transformed workload text with expressions resolved
     */
    public String getOutput() {
        return output;
    }

    /**
     * @return the Groovy binding containing all variables after expression evaluation
     */
    public Binding getBinding() {
        return binding;
    }

    /**
     * Get formatted context output showing only user-added variables.
     * Excludes framework variables that were present before script execution.
     *
     * @return formatted string representation of the binding context
     */
    public String getFormattedContext() {
        return ContextFormatter.formatContext(binding, initialVariables);
    }
}
