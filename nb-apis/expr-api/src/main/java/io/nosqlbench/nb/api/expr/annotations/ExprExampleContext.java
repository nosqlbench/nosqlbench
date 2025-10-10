package io.nosqlbench.nb.api.expr.annotations;

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
 * Defines which flavour of {@link io.nosqlbench.nb.api.expr.ExprRuntimeContext} should be used when
 * evaluating an {@link ExprExample}. This allows examples to exercise scenarios like the absence
 * of a source URI while keeping the overall evaluation harness straightforward.
 */
public enum ExprExampleContext {
    /** Use the default evaluation context, including workload parameters and a source URI. */
    DEFAULT,

    /** Use an evaluation context that omits the source URI. */
    NO_SOURCE_URI
}
