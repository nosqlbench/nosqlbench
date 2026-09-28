/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.engine.extensions.computefunctions.ComputeFunctions;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.Map;

/// Recall against the neighbors that exist: the share of a query's
/// **attainable** ground truth found in the first `k` results, where a
/// negative ground-truth entry is a sentinel for a neighbor that does
/// not exist and is left out of the denominator. The strict
/// [Recall] divides by `k` whatever the row holds, so a filtered search
/// whose predicate matches fewer than `k` rows reads low by
/// construction; this function reads what the search could have done.
/// See [ComputeFunctions#attainableRecall(int[], int[], int)].
public class AttainableRecall extends BaseRelevancyFunction {
    private final int k;

    public AttainableRecall(String name, int k, Object... labels) {
        super(name, NBLabels.forKV("k", k).and(NBLabels.forKV(labels)));
        this.k = k;
    }

    public AttainableRecall(String name, int k, Map<String, String> labels) {
        super(name, NBLabels.forKV("k", labels).andMap(labels));
        this.k = k;
    }

    @Override
    public double apply(int[] relevant, int[] actual) {
        return ComputeFunctions.attainableRecall(relevant, actual, k);
    }

    @Override
    public String getUniqueName() {
        return getName() + "_" + k;
    }
}
