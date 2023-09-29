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

package io.nosqlbench.api.config.standard;

import io.nosqlbench.api.config.NBComponent;
import io.nosqlbench.api.labels.NBLabels;

public class NBBaseComponent implements NBComponent {
    private final NBComponent parent;
    private final NBLabels labels;

    public NBBaseComponent(NBComponent parentComponent, NBLabels compomentSpecificLabelsOnly) {
        this.parent = parentComponent;
        this.labels = compomentSpecificLabelsOnly;
    }
    @Override
    public NBComponent getParent() {
        return parent;
    }

    @Override
    public NBLabels getLabels() {
        return this.parent.getLabels().and(labels);
    }
}
