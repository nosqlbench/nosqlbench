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

import java.util.List;

public record NBLabelsResult(NBLabels labels, String config, List<String> missingFields, List<String> extraneousFields) {
    public boolean isError() {
        return !missingFields.isEmpty() || !extraneousFields.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder err = new StringBuilder();
        if (!missingFields.isEmpty()) {
            err.append("The label set is missing required label names: ").append(missingFields).append("\n");
        }
        if (!extraneousFields.isEmpty()) {
            err.append("The label set has disallowed label names: ").append(extraneousFields).append("\n");
        }
        if (!err.isEmpty()) {
            err.append("This is controlled by the labeling policy: '").append(config).append("'\n");
        }
        return err.toString();
    }
}
