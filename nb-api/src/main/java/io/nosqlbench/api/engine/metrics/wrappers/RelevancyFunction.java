/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.api.engine.metrics.wrappers;

import io.nosqlbench.api.labels.NBLabeledElement;

public interface RelevancyFunction extends NBLabeledElement {
    double apply(int[] relevant, int[] actual);

    /**
     * Return a name which identifies this function in terms of its parameters. This is a temporary
     * work-around until the graphite uniqueness semantics are removed and label set uniqueness works.
     * @return A string which can be used to identify if the metric exists yet.
     */
    String getUniqueName();
}
