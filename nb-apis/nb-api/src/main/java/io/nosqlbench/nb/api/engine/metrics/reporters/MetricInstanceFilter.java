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
package io.nosqlbench.nb.api.engine.metrics.reporters;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class MetricInstanceFilter implements MetricFilter {

    private static final Set<Character> REGEX_META_CHARS = Set.of('.', '^', '$', '*', '+', '?', '(', ')', '[', ']', '{', '}', '|', '\\');

    private final List<Metric> included = new ArrayList<>();
    private final List<Pattern> includedPatterns = new ArrayList<>();
    private final List<Map<String, Pattern>> labelPatternSets = new ArrayList<>();

    public MetricInstanceFilter add(Metric metric) {
        this.included.add(metric);
        return this;
    }

    public MetricInstanceFilter addPattern(String patternSpec) {
        if (patternSpec == null) {
            return this;
        }
        String trimmed = patternSpec.trim();
        if (trimmed.isEmpty()) {
            return this;
        }
        if (trimmed.contains("=")) {
            Map<String, Pattern> labelPatterns = parseLabelPattern(trimmed);
            if (!labelPatterns.isEmpty()) {
                labelPatternSets.add(labelPatterns);
            }
        } else {
            includedPatterns.add(compileSmart(trimmed));
        }
        return this;
    }

    public boolean matches(NBLabels labels) {
        return evaluate(null, null, labels);
    }

    public boolean matches(String name, NBLabels labels) {
        return evaluate(name, null, labels);
    }

    @Override
    public boolean matches(String name, Metric metric) {
        NBLabels labels = (metric instanceof NBMetric nbMetric) ? nbMetric.getLabels() : null;
        return evaluate(name, metric, labels);
    }

    private boolean evaluate(String name, Metric metric, NBLabels labels) {
        boolean hasCriteria = !included.isEmpty() || !includedPatterns.isEmpty() || !labelPatternSets.isEmpty();
        if (!hasCriteria) {
            return true;
        }
        if (!included.isEmpty() && metric != null && included.stream().anyMatch(m -> m == metric)) {
            return true;
        }
        if (!includedPatterns.isEmpty() && name != null) {
            for (Pattern pattern : includedPatterns) {
                if (pattern.matcher(name).matches()) {
                    return true;
                }
            }
        }
        if (!labelPatternSets.isEmpty() && labels != null) {
            Map<String, String> labelMap = labels.asMap();
            for (Map<String, Pattern> labelPatterns : labelPatternSets) {
                if (matchesAllLabelPatterns(labelPatterns, labelMap)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesAllLabelPatterns(Map<String, Pattern> patterns, Map<String, String> labels) {
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            String value = labels.get(entry.getKey());
            if (value == null || !entry.getValue().matcher(value).matches()) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Pattern> parseLabelPattern(String spec) {
        Map<String, Pattern> patterns = new LinkedHashMap<>();
        String[] expressions = spec.split(";");
        for (String expression : expressions) {
            String trimmed = expression.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Label filter segment must be in key=value form: '" + trimmed + "'");
            }
            String labelName = parts[0].trim();
            String valuePattern = parts[1].trim();
            if (labelName.isEmpty()) {
                throw new IllegalArgumentException("Label name may not be empty in filter segment '" + trimmed + "'");
            }
            Pattern compiled = compileSmart(valuePattern.isEmpty() ? ".*" : valuePattern);
            patterns.put(labelName, compiled);
        }
        return patterns;
    }

    private Pattern compileSmart(String rawPattern) {
        Objects.requireNonNull(rawPattern, "pattern");
        String trimmed = rawPattern.trim();
        if (trimmed.isEmpty()) {
            return Pattern.compile(".*");
        }
        if (!containsRegexMeta(trimmed)) {
            return Pattern.compile(Pattern.quote(trimmed));
        }
        return Pattern.compile(trimmed);
    }

    private boolean containsRegexMeta(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (REGEX_META_CHARS.contains(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
