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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import io.nosqlbench.api.engine.util.Tagged;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Tags implements Tagged {

    private final Map<String, String> tags = new LinkedHashMap<>();

    @Override
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public void setTags(Map<String, String> tags) {
        this.tags.clear();
        this.tags.putAll(tags);
    }

    @SuppressWarnings("unchecked")
    public void setFieldsByReflection(Map<String, Object> propsmap) {
        Object tagsValues = propsmap.remove("tags");
        if (tagsValues != null) {
            if (tagsValues instanceof Map) {
                Map<Object, Object> tagsMap = (Map<Object, Object>) tagsValues;
                tagsMap.forEach((ko, vo) -> tags.put(ko.toString(), vo.toString()));
            } else {
                throw new RuntimeException("Invalid type for tags property: " + tags.getClass().getCanonicalName());
            }
        }
    }

}
