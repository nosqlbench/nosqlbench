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
package io.nosqlbench.engine.extensions.csvmetrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MetricInstanceFilter implements MetricFilter {

    private final List<Metric> included = new ArrayList<Metric>();
    private final List<Pattern> includedPatterns = new ArrayList<>();

    public MetricInstanceFilter add(Metric metric) {
        this.included.add(metric);
        return this;
    }

    public MetricInstanceFilter addPattern(String pattern) {
        this.includedPatterns.add(Pattern.compile(pattern));
        return this;
    }

    @Override
    public boolean matches(String name, Metric metric) {
        return (included.isEmpty() && includedPatterns.isEmpty())
                || (included.stream().anyMatch(m -> m == metric) ||
                includedPatterns.stream().anyMatch(p -> p.matcher(name).matches()));
    }
}
