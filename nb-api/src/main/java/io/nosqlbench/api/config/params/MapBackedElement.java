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

package io.nosqlbench.api.config.params;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MapBackedElement implements ElementData {

    private final Map<String, ?> map;
    private final String elementName;

    public MapBackedElement(String elementName, Map<String, ?> map) {
        this.elementName = elementName;
        this.map = map;
    }

    @Override
    public Object get(String name) {
        return map.get(name);
    }

    @Override
    public Set<String> getKeys() {
        return map.keySet();
    }

    @Override
    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    @Override
    public String getGivenName() {
        return this.elementName;
    }

    @Override
    public Object getAsCommon(String key) {
        Object found = get(key);
        if (found==null) {
            return null;
        }

        Optional<Object> converted = ElementData.asCommonType(found);
        if (converted.isPresent()) {
            return converted.get();
        }
        throw new RuntimeException("Unable to convert type '" + found.getClass().getCanonicalName() + "' to a common type.");
    }

    @Override
    public String toString() {
        return this.getGivenName() + "(" + (this.extractElementName() != null ? this.extractElementName() : "null") + "):" + map.toString();
    }
}
