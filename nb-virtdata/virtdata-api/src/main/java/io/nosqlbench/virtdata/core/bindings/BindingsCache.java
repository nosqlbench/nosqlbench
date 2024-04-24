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
package io.nosqlbench.virtdata.core.bindings;

import java.util.HashMap;
import java.util.Map;

public class BindingsCache {

    private final Bindings bindings;
    private final Map<String,Object> valuesCache = new HashMap<>();

    public BindingsCache(Bindings bindings) {
        this.bindings = bindings;
    }

    public Object getField(String fieldName, long input) {
        Object value = valuesCache.computeIfAbsent(fieldName, k -> getFieldValue(fieldName, input));
        return value;
    }

    private Object getFieldValue(String fieldName, long coordinate) {
        int i = bindings.getTemplate().getBindPointNames().indexOf(fieldName);
        if (i<0) {
            throw new RuntimeException("field name '" + fieldName + "' does not exist in bindings:" + bindings);
        }
        Object o = bindings.get(i, coordinate);
        return o;
    }

    public Map<String,Object> getCachedMap() {
        return this.valuesCache;
    }
}
