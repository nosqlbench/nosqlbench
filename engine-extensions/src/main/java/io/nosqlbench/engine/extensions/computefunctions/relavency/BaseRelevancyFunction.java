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

package io.nosqlbench.engine.extensions.computefunctions.relavency;

import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.engine.metrics.wrappers.RelevancyFunction;

public abstract class BaseRelevancyFunction implements RelevancyFunction {
    private final String name;
    private NBLabels labels;

    public BaseRelevancyFunction(String name, NBLabels labels) {
        this.name = name;
        this.labels = labels;
    }

    @Override
    public NBLabels getLabels() {
        return this.labels;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void prependLabels(NBLabeledElement prepend) {
        this.labels = prepend.getLabels().and(this.labels);

    }
}
