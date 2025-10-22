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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Tri-state metric filter used by reporters to include/exclude metrics by name, labels, or instance handle.
 *
 * <p>By default all metrics are included (allow-all). Setting {@link #defaultAccept(boolean) defaultAccept(false)} flips
 * the default to deny unless explicitly included. Clauses:</p>
 *
 * <ul>
 *   <li>{@code add(metric)} – include a specific metric instance.</li>
 *   <li>{@code exclude(metric)} – exclude a specific metric instance.</li>
 *   <li>{@code addPattern("name")} – include a metric name/regex.</li>
 *   <li>{@code addPattern("!name")} – exclude a metric name/regex (leading {@code !} or {@code -}).</li>
 *   <li>{@code addPattern("activity=write;name=op_rate")} – include by labels.</li>
 *   <li>{@code addPattern("!activity=read")} – exclude by labels.</li>
 * </ul>
 *
 * <p>The filter caches evaluation results for hot paths and clears the cache whenever clauses change.</p>
 */
public class MetricInstanceFilter implements MetricFilter {

    private static final Set<Character> REGEX_META_CHARS = Set.of('.', '^', '$', '*', '+', '?', '(', ')', '[', ']', '{', '}', '|', '\\');
    private static final int CACHE_LIMIT = 4096;

    private enum ClauseType { INCLUDE, EXCLUDE }

    private record MetricClause(ClauseType type, Metric metric) { }

    private record PatternClause(ClauseType type, Pattern pattern) { }

    private record LabelPatternClause(ClauseType type, Map<String, Pattern> patterns) { }

    private final List<MetricClause> metricClauses = new ArrayList<>();
    private final List<PatternClause> nameClauses = new ArrayList<>();
    private final List<LabelPatternClause> labelClauses = new ArrayList<>();
    private final ConcurrentHashMap<String, Boolean> memoizedMatches = new ConcurrentHashMap<>();
    private boolean defaultAccept = true;

    public MetricInstanceFilter add(Metric metric) {
        metricClauses.add(new MetricClause(ClauseType.INCLUDE, metric));
        memoizedMatches.clear();
        return this;
    }

    public MetricInstanceFilter exclude(Metric metric) {
        metricClauses.add(new MetricClause(ClauseType.EXCLUDE, metric));
        memoizedMatches.clear();
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
        ClauseType clauseType = ClauseType.INCLUDE;
        if (trimmed.startsWith("!") || trimmed.startsWith("-")) {
            clauseType = ClauseType.EXCLUDE;
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isEmpty()) {
            return this;
        }
        if (trimmed.contains("=")) {
            Map<String, Pattern> patterns = parseLabelPattern(trimmed);
            if (!patterns.isEmpty()) {
                labelClauses.add(new LabelPatternClause(clauseType, patterns));
            }
        } else {
            nameClauses.add(new PatternClause(clauseType, compileSmart(trimmed)));
        }
        memoizedMatches.clear();
        return this;
    }

    public MetricInstanceFilter excludePattern(String patternSpec) {
        return addPattern("!" + patternSpec);
    }

    /**
     * Sets the default result when no inclusive clause matches. By default the filter is permissive (accept all unless
     * excluded). Calling {@code defaultAccept(false)} switches to a deny-by-default posture where only explicit include
     * clauses are admitted.
     */
    public MetricInstanceFilter defaultAccept(boolean accept) {
        this.defaultAccept = accept;
        memoizedMatches.clear();
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
        if (metricClauses.isEmpty() && nameClauses.isEmpty() && labelClauses.isEmpty()) {
            return true;
        }
        String key = cacheKey(name, metric, labels);
        Boolean cached = memoizedMatches.get(key);
        if (cached != null) {
            return cached;
        }

        boolean matchedInclude = false;
        boolean matchedExclude = false;

        if (metric != null) {
            for (MetricClause clause : metricClauses) {
                if (clause.metric() == metric) {
                    if (clause.type() == ClauseType.EXCLUDE) {
                        matchedExclude = true;
                    } else {
                        matchedInclude = true;
                    }
                }
            }
        }

        if (name != null) {
            for (PatternClause clause : nameClauses) {
                if (clause.pattern().matcher(name).matches()) {
                    if (clause.type() == ClauseType.EXCLUDE) {
                        matchedExclude = true;
                    } else {
                        matchedInclude = true;
                    }
                }
            }
        }

        if (!labelClauses.isEmpty() && labels != null) {
            Map<String, String> labelMap = labels.asMap();
            for (LabelPatternClause clause : labelClauses) {
                if (matchesAllLabelPatterns(clause.patterns(), labelMap)) {
                    if (clause.type() == ClauseType.EXCLUDE) {
                        matchedExclude = true;
                    } else {
                        matchedInclude = true;
                    }
                }
            }
        }

        if (matchedExclude) {
            memoize(key, false);
            return false;
        }

        boolean result;
        if (hasIncludeClauses()) {
            result = matchedInclude;
        } else {
            result = defaultAccept;
        }
        memoize(key, result);
        return result;
    }

    private void memoize(String key, boolean value) {
        if (memoizedMatches.size() >= CACHE_LIMIT) {
            memoizedMatches.clear();
        }
        memoizedMatches.put(key, value);
    }

    private boolean hasIncludeClauses() {
        return metricClauses.stream().anyMatch(clause -> clause.type() == ClauseType.INCLUDE)
            || nameClauses.stream().anyMatch(clause -> clause.type() == ClauseType.INCLUDE)
            || labelClauses.stream().anyMatch(clause -> clause.type() == ClauseType.INCLUDE);
    }

    private String cacheKey(String name, Metric metric, NBLabels labels) {
        String identity;
        if (name != null) {
            identity = name;
        } else if (metric != null) {
            identity = metric.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(metric));
        } else {
            identity = "<anon>";
        }
        String labelIdentity;
        if (labelClauses.isEmpty()) {
            labelIdentity = "<labels-na>";
        } else if (labels != null) {
            labelIdentity = labels.linearizeAsMetrics();
        } else {
            labelIdentity = "<no-labels>";
        }
        return identity + '|' + labelIdentity;
    }

    private boolean matchesAllLabelPatterns(Map<String, Pattern> patterns, Map<String, String> labelMap) {
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            String value = labelMap.get(entry.getKey());
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
