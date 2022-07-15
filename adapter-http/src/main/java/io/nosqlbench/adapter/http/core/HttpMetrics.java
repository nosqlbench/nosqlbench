/*
 * Copyright (c) 2022 nosqlbench
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
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;

public class HttpMetrics implements NBNamedElement {
    private final HttpSpace space;
    final Histogram statusCodeHistogram;

    public HttpMetrics(HttpSpace space) {
        this.space = space;
        statusCodeHistogram = ActivityMetrics.histogram(this, "statuscode",space.getHdrDigits());
    }

    @Override
    public String getName() {
        return "http"+(space.getName().equals("default")?"":"-"+space.getName());
    }
}
