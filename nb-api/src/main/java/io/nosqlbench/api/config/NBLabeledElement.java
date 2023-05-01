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

package io.nosqlbench.api.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public interface NBLabeledElement {

    NBLabeledElement EMPTY = forMap(Map.of());

    Map<String, String> getLabels();

    /**
     * TODO: Should throw an error when new keys are duplicated
     * @param keyvalues
     * @return
     */
    default Map<String, String> getLabelsAnd(final String... keyvalues) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>(this.getLabels());
        for (int idx = 0; idx < keyvalues.length; idx+=2) map.put(keyvalues[idx], keyvalues[idx + 1]);
        return map;
    }

//    default NBLabeledElement and(String... keyvalues) {
//
//    }

    default Map<String, String> getLabelsAnd(final Map<String,String> extra) {
        final LinkedHashMap<String,String> map = new LinkedHashMap<>(this.getLabels());
        map.putAll(extra);
        return map;
    }

    static MapLabels forMap(final Map<String,String> labels) {
        return new MapLabels(labels);
    }

    class MapLabels implements NBLabeledElement {
        private final Map<String, String> labels;

        public MapLabels(final Map<String,String> labels) {
            this.labels = labels;
        }

        @Override
        public Map<String, String> getLabels() {
            return this.labels;
        }
    }

    /**
     * Create a single String representation of the label set, preserving key order,
     * with optional additional labels, in the form of:
     * <pre>{@code
     *  key1:value1,key2:value2,...
     * }</pre>
     * @param and
     * @return
     */
    default String linearizeLabels(final Map<String,String> and) {
        final StringBuilder sb= new StringBuilder();
        final Map<String, String> allLabels = getLabelsAnd(and);
        final ArrayList<String> sortedLabels = new ArrayList<>(allLabels.keySet());
        for (final String label : sortedLabels) sb.append(label).append(':').append(allLabels.get(label)).append(',');
        sb.setLength(sb.length()-",".length());
        return sb.toString();
    }

    /**
     * Equivalent to {@link #linearizeLabels(Map)}, except that additional key-value pairs can
     * be expressed as a pairs of Strings in the argument list.
     * @param and - An even numbered list of strings as key1, value1, key2, value2, ...
     * @return A linearized string representation
     */
    default String linearizeLabels(final String... and) {
        return this.linearizeLabels(this.getLabelsAnd(and));
    }

    default String linearizeLabelsByValueGraphite(final String... and) {
        return this.linearizeLabelsByValueDelim(".",and);
    }
    /**
     * Create a single String representation of the label set, preserving key order,
     * with optional additional labels and the specified delimiter,
     * using <EM>only</EM> values, in the form of:
     * <pre>{@code
     *  value1-value2...
     * }</pre>
     * @param and
     * @return
     */
    default String linearizeLabelsByValueDelim(final String delim, final String... and) {
        final Map<String, String> fullLabels = this.getLabelsAnd(and);
        final StringBuilder sb = new StringBuilder();
        for (final String labelName : fullLabels.keySet()) sb.append(fullLabels.get(labelName)).append(delim);
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

}
