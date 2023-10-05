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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponent;

import java.util.Map;

public abstract class BaseDiagTask implements DiagTask {
    private NBLabeledElement parentLabels;
    private String name;
    protected NBComponent parent;


    @Override
    public abstract Map<String, Object> apply(Long cycle, Map<String, Object> opstate);

    @Override
    public NBLabels getLabels() {
        return parentLabels.getLabels();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setLabelsFrom(NBLabeledElement labeledElement) {
        this.parentLabels = labeledElement;
    }

    @Override
    public NBLabeledElement getParentLabels() {
        return parentLabels;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void applyParentComponent(NBComponent parent) {
        this.parent=parent;
    }
}
