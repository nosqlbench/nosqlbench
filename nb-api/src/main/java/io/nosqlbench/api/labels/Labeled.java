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

package io.nosqlbench.api.labels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Labeled {
    Map<String, String> getLabels();

    default Map<String, String> getLabelsAnd(String... keyvalues) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(getLabels());
        for (int idx = 0; idx < keyvalues.length; idx+=2) {
            map.put(keyvalues[idx],keyvalues[idx+1]);
        }
        return map;
    }

    default Map<String, String> getLabelsAnd(Map<String,String> extra) {
        LinkedHashMap<String,String> map = new LinkedHashMap<>(getLabels());
        map.putAll(extra);
        return map;
    }

    static MapLabels forMap(Map<String,String> labels) {
        return new MapLabels(labels);
    }

    class MapLabels implements Labeled {
        private final Map<String, String> labels;

        public MapLabels(Map<String,String> labels) {
            this.labels = labels;
        }

        @Override
        public Map<String, String> getLabels() {
            return labels;
        }
    }

    default String linearized(Map<String,String> and) {
        StringBuilder sb= new StringBuilder();
        Map<String, String> allLabels = this.getLabelsAnd(and);
        ArrayList<String> sortedLabels = new ArrayList<>(allLabels.keySet());
        Collections.sort(sortedLabels);
        for (String label : sortedLabels) {
            sb.append(label).append(":").append(allLabels.get(label)).append((","));
        }
        sb.setLength(sb.length()-",".length());
        return sb.toString();
    }

    default String linearized(String... and) {
        return linearized(getLabelsAnd(and));
    }
}
