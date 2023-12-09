/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.http.core;

import com.codahale.metrics.Histogram;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;

public class HttpMetrics implements NBLabeledElement {
    private final NBComponent parent;
    private final HttpSpace space;
    final Histogram statusCodeHistogram;

    public HttpMetrics(NBComponent parent, HttpSpace space) {
        this.parent = parent;
        this.space = space;
        statusCodeHistogram = parent.create().histogram("statuscode",space.getHdrDigits());
    }

    public String getName() {
        return "http"+("default".equals(this.space.getSpaceName())?"": '-' + space.getSpaceName());
    }

    @Override
    public NBLabels getLabels() {
        return space.getLabels();
    }
}
