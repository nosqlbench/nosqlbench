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

import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.labels.NBLabels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class NBBaseComponent extends NBBaseComponentMetrics implements NBComponent {
    private final NBComponent parent;
    private final List<NBComponent> children = new ArrayList<>();
    private final NBLabels labels;

    public NBBaseComponent(NBComponent parentComponent) {
        this(parentComponent,NBLabels.forKV());
    }
    public NBBaseComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly) {
        this.labels = componentSpecificLabelsOnly;
        if (parentComponent!=null) {
            parent = parentComponent;
            parent.attach(this);
        } else {
            parent=null;
        }
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

    @Override
    public NBMetric lookupMetricInTree(String name) {
        Iterator<NBComponent> tree = NBComponentTraversal.traverseBreadth(this);
        while (tree.hasNext()) {
            NBComponent c = tree.next();
            NBMetric metric = c.lookupMetric(name);
            if (metric!=null) return metric;
        }
        return null;
    }

    @Override
    public List<NBMetric> findMetricsInTree(String pattern) {
        Iterator<NBComponent> tree = NBComponentTraversal.traverseBreadth(this);
        List<NBMetric> found = new ArrayList<>();
        while (tree.hasNext()) {
            NBComponent c = tree.next();
            found.addAll(c.findMetrics(pattern));
        }
        return found;
    }
}
