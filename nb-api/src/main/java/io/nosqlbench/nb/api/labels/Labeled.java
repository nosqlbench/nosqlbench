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

package io.nosqlbench.nb.api.labels;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Labeled {
    Map<String, String> getLabels();

    default Map<String, String> getLabelsAnd(String... keyvalues) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(getLabels());
        for (int idx = 0; idx < keyvalues.length; idx+=2) {
            map.put(keyvalues[0],keyvalues[1]);
        }
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
}
