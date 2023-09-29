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

package io.nosqlbench.components;

import io.nosqlbench.api.labels.NBLabels;

import java.util.*;

public class NBBaseComponent implements NBComponent {
    private final NBComponent parent;
    private final List<NBComponent> children = new ArrayList<>();
    private final NBLabels labels;

    public NBBaseComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly) {
        this.labels = componentSpecificLabelsOnly;
        this.parent = parentComponent;
        if (this.parent!=null) { parentComponent.attach(this);}
    }
    @Override
    public NBComponent getParent() {
        return parent;
    }

    @Override
    public NBComponent attach(NBComponent... children) {
        this.children.addAll(Arrays.asList(children));
        return this;
    }

    @Override
    public NBComponent detach(NBComponent... children) {
        this.children.removeAll(Arrays.asList(children));
        return this;
    }

    @Override
    public List<NBComponent> getChildren() {
        return children;
    }

    @Override
    public NBLabels getLabels() {
        return (this.parent==null) ? labels : this.parent.getLabels().and(labels);
    }

}
