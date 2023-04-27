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

    Map<String, String> getLabels();

    /**
     * TODO: Should throw an error when new keys are duplicated
     * @param keyvalues
     * @return
     */
    default Map<String, String> getLabelsAnd(String... keyvalues) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(getLabels());
        for (int idx = 0; idx < keyvalues.length; idx+=2) {
            map.put(keyvalues[idx], keyvalues[idx + 1]);
        }
        return map;
    }

//    default NBLabeledElement and(String... keyvalues) {
//
//    }

    default Map<String, String> getLabelsAnd(Map<String,String> extra) {
        LinkedHashMap<String,String> map = new LinkedHashMap<>(getLabels());
        map.putAll(extra);
        return map;
    }

    static MapLabels forMap(Map<String,String> labels) {
        return new MapLabels(labels);
    }

    class MapLabels implements NBLabeledElement {
        private final Map<String, String> labels;

        public MapLabels(Map<String,String> labels) {
            this.labels = labels;
        }

        @Override
        public Map<String, String> getLabels() {
            return labels;
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
    default String linearized(Map<String,String> and) {
        StringBuilder sb= new StringBuilder();
        Map<String, String> allLabels = this.getLabelsAnd(and);
        ArrayList<String> sortedLabels = new ArrayList<>(allLabels.keySet());
        for (String label : sortedLabels) {
            sb.append(label).append(':').append(allLabels.get(label)).append(',');
        }
        sb.setLength(sb.length()-",".length());
        return sb.toString();
    }

    /**
     * Equivalent to {@link #linearized(Map)}, except that additional key-value pairs can
     * be expressed as a pairs of Strings in the argument list.
     * @param and - An even numbered list of strings as key1, value1, key2, value2, ...
     * @return A linearized string representation
     */
    default String linearized(String... and) {
        return linearized(getLabelsAnd(and));
    }

    default String linearizedByValueGraphite(String... and) {
        return linearizedByValueDelim(".",and);
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
    default String linearizedByValueDelim(String delim, String... and) {
        Map<String, String> fullLabels = getLabelsAnd(and);
        StringBuilder sb = new StringBuilder();
        for (String labelName : fullLabels.keySet()) {
            sb.append(fullLabels.get(labelName)).append(delim);
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

}
