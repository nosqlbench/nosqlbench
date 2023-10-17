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

package io.nosqlbench.api.labels;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NBLabelUtils {

    public static NBLabels common(List<NBLabels> labelsets) {
        if (labelsets.isEmpty()) {
            return NBLabels.forKV();
        }

        NBLabels common = labelsets.getFirst();

        for (NBLabels labelset : labelsets) {
            common = common.intersection(labelset);
        }
        return common;
    }

    public static <T extends NBLabeledElement> NBLabels commonLabels(List<T> elements) {
        return common(elements.stream().map(NBLabeledElement::getLabels).collect(Collectors.toList()));
    }
}
