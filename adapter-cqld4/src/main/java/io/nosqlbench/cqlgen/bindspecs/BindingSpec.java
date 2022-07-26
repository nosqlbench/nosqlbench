/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.cqlgen.bindspecs;

import io.nosqlbench.api.labels.Labeled;

public interface BindingSpec {

    /**
     * Is this binding intended to be for a limited population?
     * If so, the value will be the maximum cardinality of values which the binding
     * is allowed to produce.
     * @return The effective cardinality, which could be {@link Double#POSITIVE_INFINITY}
     */
    default double getCardinality() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * The fully qualified name of the entity for which the binding values pertain.
     * This is
     * @return
     */
    Labeled getTarget();

    String getTypedef();
}
